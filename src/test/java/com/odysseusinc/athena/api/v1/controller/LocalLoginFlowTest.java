package com.odysseusinc.athena.api.v1.controller;

import com.jayway.jsonpath.JsonPath;
import com.odysseusinc.athena.TestConfiguration;
import com.odysseusinc.athena.glue.LocalEnvironmentInitializer;
import com.odysseusinc.athena.service.registration.AccountEmailQueue;
import jakarta.servlet.Filter;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import java.net.URI;
import com.odysseusinc.athena.service.mail.AccountMail;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestConfiguration.class, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "athena.recovery.mail.poll-delay=PT1H",
        "athena.url=https://athena.example.org"})
@ContextConfiguration(initializers = LocalEnvironmentInitializer.class)
@ActiveProfiles("test")
class LocalLoginFlowTest {
    private static final String HEADER = "Athena-Auth-Token";
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter security;
    @Autowired @Qualifier("dataSourceAthenaDB") DataSource dataSource;
    @Autowired AccountEmailQueue queue;
    @MockitoBean AccountMail mail;
    @MockitoBean(name = "testJavaMailSender") JavaMailSender notifications;
    private MockMvc mvc;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/users/login").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String token(String email) throws Exception {
        return browserToken(email, "Local-password-93!");
    }

    private String browserToken(String email, String password) throws Exception {
        String response = login(email, password).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.result.token");
    }

    private void register(String email, String password) throws Exception {
        mvc.perform(post("/api/v1/users").contentType("application/json").content("""
                {"email":"%s","password":"%s","firstname":"Native","lastname":"Flow",
                 "organization":"Example","professionalTypeId":1,
                 "address":{"address1":"1 Main St","city":"Lisbon","zipCode":"1000","mobile":"1234567","country":{"isoCode":"PT"}}}
                """.formatted(email, password))).andExpect(status().isOk());
    }

    @Test void loginLimitsRecognizeTheSameEmailAndReleaseItAfterExpiry() throws Exception {
        String email = "login-limit@example.org";
        for (int attempt = 0; attempt < 10; attempt++) {
            login(email, "incorrect").andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/users/login").with(request -> {
            request.setRemoteAddr("192.0.2.80");
            return request;
        }).contentType("application/json").content("""
                {"email":" LOGIN-LIMIT@EXAMPLE.ORG ","password":"incorrect"}
                """)).andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", org.hamcrest.Matchers.matchesPattern("[1-9][0-9]*")))
                .andExpect(jsonPath("$.errorMessage").isString());

        JdbcClient.create(dataSource).sql("""
                UPDATE account_request_limits SET expires_at = now() - interval '1 second'
                WHERE scope = 'login-email' AND subject_hash = encode(sha256(convert_to(?, 'UTF8')), 'hex')
                """).param(email).update();
        login(email, "incorrect").andExpect(status().isUnauthorized());
    }

    @Test void clientBudgetsAreIndependentAndBlockAttemptsAcrossEmails() {
        var limits = new AccountRequestLimits(dataSource, 900, 1, 10, 1, 5, 1, 1, 1, 1);
        String client = "192.0.2.96";
        assertEquals(0, limits.loginDelay(client, "first-limit@example.org"));
        org.junit.jupiter.api.Assertions.assertTrue(limits.loginDelay(client, "second-limit@example.org") > 0);
        org.junit.jupiter.api.Assertions.assertTrue(limits.allowReminder(client, "first-limit@example.org"));
        org.junit.jupiter.api.Assertions.assertFalse(limits.allowReminder(client, "second-limit@example.org"));
        assertEquals(0, limits.registrationDelay(client, "register-limit@example.org"));
        org.junit.jupiter.api.Assertions.assertTrue(limits.registrationDelay(client, "other-limit@example.org") > 0);
        org.junit.jupiter.api.Assertions.assertTrue(limits.registrationDelay("192.0.2.98", "REGISTER-LIMIT@example.org") > 0);
        assertEquals(0, limits.confirmationDelay(client));
        org.junit.jupiter.api.Assertions.assertTrue(limits.confirmationDelay(client) > 0);
        assertEquals(0, limits.resetDelay(client));
        org.junit.jupiter.api.Assertions.assertTrue(limits.resetDelay(client) > 0);
        assertEquals(0, limits.loginDelay("192.0.2.97", "second-limit@example.org"));
    }

    @Test void reminderDeliveryCanRetryAfterMailFailureAndAfterLinkExpiry() throws Exception {
        String email = "delivery-retry@example.org";
        register(email, "Original-password-82!");
        var recipient = new AccountMail.Recipient(email, "Native");
        org.mockito.Mockito.doThrow(new org.springframework.mail.MailSendException("fixture"))
                .doNothing().when(mail).sendPasswordReset(eq(recipient), org.mockito.ArgumentMatchers.any());
        String acknowledgement = remind(email);
        assertEquals(acknowledgement, remind(email));
        var sent = ArgumentCaptor.forClass(URI.class);
        verify(mail, org.mockito.Mockito.never()).sendPasswordReset(eq(recipient), org.mockito.ArgumentMatchers.any());
        queue.deliverPending();
        queue.deliverPending();
        verify(mail).sendPasswordReset(eq(recipient), sent.capture());
        URI failedLink = sent.getValue();
        JdbcClient.create(dataSource).sql("UPDATE users_data SET reset_delivery_due_at = now() WHERE email = ?")
                .param(email).update();
        queue.deliverPending();
        verify(mail, org.mockito.Mockito.times(2)).sendPasswordReset(eq(recipient), sent.capture());
        assertEquals(failedLink, sent.getValue());
        String firstToken = sent.getValue().getFragment().split("&token=")[1];

        JdbcClient.create(dataSource).sql("UPDATE users_data SET reset_expires_at = now() WHERE email = ?")
                .param(email).update();
        assertEquals(acknowledgement, remind(email));
        queue.deliverPending();
        verify(mail, org.mockito.Mockito.times(3)).sendPasswordReset(eq(recipient), sent.capture());
        String replacement = sent.getValue().getFragment().split("&token=")[1];
        assertNotEquals(firstToken, replacement);
        reset(email, firstToken).andExpect(status().isBadRequest());
        reset(email, replacement).andExpect(status().isOk());
    }

    @Test void legacyAuthRoutesAreUnavailable() throws Exception {
        for (String path : new String[]{"/auth/sso", "/auth/slo", "/auth/saml-metadata", "/auth/logged-out",
                "/auth/complete", "/saml2/authenticate/athena", "/login/saml2/sso/athena"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }
        mvc.perform(post("/auth/callback")).andExpect(status().isNotFound());
        org.junit.jupiter.api.Assertions.assertFalse(context.containsBean("relyingPartyRegistrationRepository"));
        org.junit.jupiter.api.Assertions.assertFalse(context.containsBean("centralRestTemplate"));
    }

    @Test void registerConfirmLoginReadPrincipalAndLogout() throws Exception {
        String email = "native-flow@example.org";
        register(email, "Local-password-93!");
        String pending = login(email, "Local-password-93!").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertEquals(pending, login("missing@example.org", "Local-password-93!").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString());
        var sent = ArgumentCaptor.forClass(URI.class);
        queue.deliverPending();
        verify(mail).sendConfirmation(eq(new AccountMail.Recipient(email, "Native")), sent.capture());
        var url = sent.getValue();
        String confirmToken = url.getFragment().substring("token=".length());
        mvc.perform(get("/api/v1/users/confirm-email").queryParam("token", confirmToken))
                .andExpect(status().isSeeOther()).andExpect(redirectedUrl("/auth/confirm-email#token=" + confirmToken));
        login(email, "Local-password-93!").andExpect(status().isUnauthorized());
        confirm(confirmToken).andExpect(status().isOk());
        login(email, "incorrect").andExpect(status().isUnauthorized());
        String jwt = token(email.toUpperCase(java.util.Locale.ROOT));
        mvc.perform(get("/api/v1/users/me").header(HEADER, jwt)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(email));
        mvc.perform(post("/api/v1/users/logout").header(HEADER, jwt)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/users/me").header(HEADER, jwt)).andExpect(status().isForbidden());
        String next = token(email);
        assertNotEquals(jwt, next);
        mvc.perform(get("/api/v1/users/me").header(HEADER, next)).andExpect(status().isOk());
    }

    @Test void completedResetConfirmsPendingRegistration() throws Exception {
        String email = "pending-reset@example.org";
        String originalPassword = "Original-password-82!";
        register(email, originalPassword);
        remind(email);
        queue.deliverPending();
        var sent = ArgumentCaptor.forClass(URI.class);
        var recipient = new AccountMail.Recipient(email, "Native");
        verify(mail).sendConfirmation(eq(recipient), sent.capture());
        String confirmationToken = sent.getValue().getFragment().substring("token=".length());
        verify(mail).sendPasswordReset(eq(recipient), sent.capture());
        String resetToken = sent.getValue().getFragment().split("&token=")[1];

        reset(email, "invalid-token").andExpect(status().isBadRequest());
        login(email, originalPassword).andExpect(status().isUnauthorized());
        reset(email, resetToken).andExpect(status().isOk());
        mvc.perform(get("/api/v1/users/me").header(HEADER, token(email)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(email));
        login(email, originalPassword).andExpect(status().isUnauthorized());
        reset(email, resetToken).andExpect(status().isBadRequest());
        confirm(confirmationToken)
                .andExpect(status().isBadRequest());
    }

    @Test void importedAccountRecoversUsingOnlyAccountMail() throws Exception {
        var jdbc = JdbcClient.create(dataSource);
        String email = "reset+legacy@example.org";
        long id = jdbc.sql("""
                INSERT INTO users (id,login,origin,email) VALUES (nextval('users_seq'),:email,'Arachne',:email)
                RETURNING id
                """).param("email", email).query(Long.class).single();
        jdbc.sql("INSERT INTO users_data (athena_user_id, arachne_user_id, email, password) VALUES (?, ?, ?, ?)")
                .params(id, 234567L, email, new BCryptPasswordEncoder().encode("Old-password-93!")).update();

        String oldPassword = "Old-password-93!";
        String firstSession = browserToken(email, oldPassword);
        String secondSession = browserToken(email, oldPassword);
        assertNotEquals(firstSession, secondSession);
        String missing = remind("unknown@example.org");
        queue.deliverPending();
        org.mockito.Mockito.verifyNoInteractions(mail);
        assertEquals(missing, remind(email));
        // Cross the reminder quota using the same account in different casing.
        for (int i = 0; i < 6; i++) {
            assertEquals(missing, remind(email.toUpperCase(java.util.Locale.ROOT)));
        }
        var sent = ArgumentCaptor.forClass(URI.class);
        queue.deliverPending();
        verify(mail).sendPasswordReset(eq(new AccountMail.Recipient(email, null)), sent.capture());
        var link = sent.getValue();
        assertEquals("/auth/reset-password", link.getPath());
        org.junit.jupiter.api.Assertions.assertTrue(link.getRawFragment().startsWith("email=reset%2Blegacy%40example.org&token="));
        String token = link.getFragment().split("&token=")[1];
        reset("another@example.org", token).andExpect(status().isBadRequest());
        for (String session : new String[]{firstSession, secondSession}) {
            mvc.perform(get("/api/v1/users/me").header(HEADER, session)).andExpect(status().isOk());
        }
        reset(email, token).andExpect(status().isOk());
        for (String session : new String[]{firstSession, secondSession}) {
            mvc.perform(get("/api/v1/users/me").header(HEADER, session)).andExpect(status().isForbidden())
                    .andExpect(header().string("Athena-Session-Invalid", "true"));
        }
        reset(email, token).andExpect(status().isBadRequest());
        login(email, "Old-password-93!").andExpect(status().isUnauthorized());
        String jwt = token(email);
        mvc.perform(get("/api/v1/users/me").header(HEADER, jwt)).andExpect(jsonPath("$.id").value(id));
        org.mockito.Mockito.verifyNoInteractions(notifications);
    }

    private ResultActions confirm(String token) throws Exception {
        return mvc.perform(post("/api/v1/users/confirm-email").contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"));
    }

    private String remind(String email) throws Exception {
        return mvc.perform(post("/api/v1/users/remind-password").contentType("application/json")
                .content("{\"email\":\"" + email + "\"}")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions reset(String email, String token) throws Exception {
        return mvc.perform(post("/api/v1/users/reset-password").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"token\":\"" + token + "\",\"password\":\"Local-password-93!\"}"));
    }

    @Test void importedCredentialUsesOriginalAthenaIdentity() throws Exception {
        var jdbc = JdbcClient.create(dataSource);
        long userId = jdbc.sql("""
                INSERT INTO users (id,login,origin,email) VALUES (nextval('users_seq'),'legacy-login','Arachne','legacy@example.org')
                RETURNING id
                """).query(Long.class).single();
        jdbc.sql("INSERT INTO users_data (athena_user_id, arachne_user_id, email, password) VALUES (?, ?, ?, ?)")
                .params(userId, 123456L, "legacy@example.org", new BCryptPasswordEncoder().encode("Local-password-93!")).update();
        mvc.perform(get("/api/v1/users/me").header(HEADER, token("legacy@example.org")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("legacy-login")).andExpect(jsonPath("$.origin").value("Arachne"));
    }
}
