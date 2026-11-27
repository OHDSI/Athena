package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.service.registration.RegistrationService;
import com.odysseusinc.athena.service.registration.AccountEmailQueue;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.MailSendException;
import java.net.URI;
import com.odysseusinc.athena.service.mail.AccountMail;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerRegistrationTest {

    @ClassRule
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2");
    private static final AccountMail mail = mock(AccountMail.class);
    private static AnnotationConfigApplicationContext context;
    private static JdbcClient db;
    private static MockMvc mvc;

    @EnableTransactionManagement
    static class Config {
        @Bean AccountEmailQueue queue(DataSource source) {
            return new AccountEmailQueue(source, mail, "https://athena.example.org", "test-signing-key");
        }
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }
        @Bean DataSourceTransactionManager accountJdbcTransactionManager(DataSource source) {
            return new DataSourceTransactionManager(source);
        }
        @Bean RegistrationService registrationService(DataSource source, AccountEmailQueue queue) {
            return new RegistrationService(source, queue, "ROLE_USER");
        }
    }

    @BeforeClass public static void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        DataSource source = context.getBean(DataSource.class);
        Flyway.configure().dataSource(source).locations("db/migration/share", "db/migration/athena").load().migrate();
        db = JdbcClient.create(source);
        mvc = MockMvcBuilders.standaloneSetup(new RegistrationController(context.getBean(RegistrationService.class),
                new AccountRequestLimits(source, 900, 100, 10, 30, 5, 60, 100, 20))).build();
    }

    @AfterClass public static void close() {
        if (context != null) context.close();
    }

    @Before public void resetMail() {
        reset(mail);
    }

    private String payload(String email) {
        return """
                {"firstname":"Clara","lastname":"Jones","organization":"Example","department":"Engineering",
                 "professionalTypeId":1,"email":"%s","password":"Known-password-47!",
                 "address":{"address1":"1 Main Street","city":"Oakland","zipCode":"94612","mobile":"5551234",
                            "country":{"isoCode":"US"},"stateProvince":{"isoCode":"US-CA"}}}
                """.formatted(email);
    }

    private ResultActions register(String body) throws Exception {
        return mvc.perform(post("/api/v1/users").contentType("application/json").content(body));
    }

    private ResultActions confirm(String token) throws Exception {
        return mvc.perform(post("/api/v1/users/confirm-email").contentType("application/json")
                .content("{\"token\":\"" + token + "\"}"));
    }

    private String emailedToken(String email) {
        context.getBean(AccountEmailQueue.class).deliverPending();
        var url = ArgumentCaptor.forClass(URI.class);
        verify(mail).sendConfirmation(eq(new AccountMail.Recipient(email, "Clara")), url.capture());
        String link = url.getValue().toString();
        assertTrue(link.startsWith("https://athena.example.org/auth/confirm-email#token="));
        return link.substring(link.indexOf('=') + 1);
    }

    @Test public void storesBcryptWithProfileAndConfirmsOnce() throws Exception {
        String email = "signup@example.org";
        register(payload(email)).andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value(0))
                .andExpect(jsonPath("$.result").isEmpty());
        var row = db.sql("""
                SELECT d.password, d.confirmation_token_hash FROM users_data d
                JOIN users u ON u.id = d.athena_user_id
                JOIN users_roles ur ON ur.user_id = u.id JOIN roles r ON r.id = ur.role_id
                JOIN countries c ON c.id = d.country_id JOIN states_provinces s ON s.id = d.state_province_id
                WHERE u.login = :email AND u.origin = 'Athena' AND r.name = 'ROLE_USER'
                AND u.firstname = 'Clara' AND d.department = 'Engineering' AND d.professional_type_id = 1
                AND d.city = 'Oakland' AND c.alfa2 = 'US' AND s.iso_code = 'US-CA'
                AND d.email_confirmed_at IS NULL
                """).param("email", email).query().singleRow();
        assertTrue(new BCryptPasswordEncoder().matches("Known-password-47!", (String) row.get("password")));
        String token = emailedToken(email);
        assertNotEquals(token, row.get("confirmation_token_hash"));
        mvc.perform(get("/api/v1/users/confirm-email").param("token", token))
                .andExpect(redirectedUrl("/auth/confirm-email#token=" + token));
        confirm(token).andExpect(status().isOk());
        confirm(token).andExpect(status().isBadRequest());
        register(payload("SIGNUP@example.org")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validatorErrors.email").isString());
    }

    @Test public void reportsFieldErrorsWithoutClaimingExistingUsers() throws Exception {
        register(payload("admin@ADMIN.ru")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(5));
        register("{}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(3))
                .andExpect(jsonPath("$.validatorErrors.password").isString());
        register(payload("invalid@example.org").replace("Known-password-47!", "é".repeat(37)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validatorErrors.password").isString());
        register(payload("invalid@example.org").replace("\"US\"", "\"PT\"")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validatorErrors['address.stateProvince']").isString());
        register(payload("invalid@example.org").replace("\"professionalTypeId\":1", "\"professionalTypeId\":100"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validatorErrors.professionalTypeId").isString());
        verifyNoInteractions(mail);
    }

    @Test public void failedConfirmationDeliveryRetriesTheOriginalLink() throws Exception {
        String email = "retry@example.org";
        register(payload(email)).andExpect(status().isOk());
        verifyNoInteractions(mail);
        doThrow(new MailSendException("Unavailable")).doNothing().when(mail).sendConfirmation(any(), any());
        var queue = context.getBean(AccountEmailQueue.class);
        queue.deliverPending();
        db.sql("UPDATE users_data SET confirmation_delivery_due_at = now() WHERE email = ?").param(email).update();
        queue.deliverPending();
        var links = ArgumentCaptor.forClass(URI.class);
        verify(mail, times(2)).sendConfirmation(any(), links.capture());
        assertEquals(links.getAllValues().get(0), links.getValue());
        confirm(links.getValue().getFragment().substring(6)).andExpect(status().isOk());
    }

    @Test public void expiredLinkCanBeRenewedWithoutChangingCredentials() throws Exception {
        String email = "expired@example.org";
        register(payload(email)).andExpect(status().isOk());
        String oldToken = emailedToken(email);
        db.sql("UPDATE users_data SET confirmation_expires_at = now() - interval '1 minute' WHERE email = :email")
                .param("email", email).update();
        confirm(oldToken).andExpect(status().isBadRequest());
        reset(mail);
        register(payload(email).replace("Known-password-47!", "Different-password-73!")).andExpect(status().isBadRequest());
        register(payload(email)).andExpect(status().isOk());
        confirm(emailedToken(email)).andExpect(status().isOk());
    }
}
