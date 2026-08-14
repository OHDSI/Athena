/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Created: August 14, 2026
 *
 */

package com.odysseusinc.athena.config;

import com.odysseusinc.athena.security.apitoken.ApiTokenAuthenticationFilter;
import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import com.odysseusinc.athena.security.jwt.JwtAuthenticationFilter;
import com.odysseusinc.athena.security.jwt.JwtTokenService;
import jakarta.servlet.Filter;
import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.util.ServletRequestPathUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The wiring invariants in {@link WebSecurityConfig} that nothing else checks.
 * <p>
 * Each of these was a real defect on this branch, and in both cases the fix lives in a
 * single argument that reads as arbitrary unless you know the failure. A comment records
 * why; this records it in a form that fails the build when someone changes it back.
 * <p>
 * {@code LogoutRevocationTest} cannot stand in for the ordering assertion below. The
 * handler falls back to reading the token off the request when the security context is
 * empty ({@code fromContext.or(...)}), which is deliberate — but it means the handler
 * revokes correctly whether or not the filter runs early enough to populate the context.
 * Both the fixed and the broken wiring pass that test. Only the position assertion here
 * distinguishes them.
 * <p>
 * The chains are built directly from the {@code @Bean} methods rather than from a running
 * context: the filters are never executed, only located, so their collaborators can be
 * null and no database, servlet container or identity provider is needed.
 */
public class SecurityFilterChainWiringTest {

    /** Long enough to satisfy the 32-byte minimum JwtTokenService enforces on the salt. */
    private static final String SALT = "test-salt-for-filter-chain-wiring-assertions";

    private static final String TOKEN_HEADER = "Authorization";

    private final WebSecurityConfig config = new WebSecurityConfig();

    /**
     * The fix for the logout regression: the JWT filter has to be inserted before
     * {@link LogoutFilter}, not before {@code BasicAuthenticationFilter}. Spring Security
     * places BasicAuthenticationFilter far later in the chain, so authenticating there
     * left the context empty for the whole of a logout request and the token stayed valid
     * for its full lifetime while logout still answered 200.
     */
    @Test
    public void jwtFilterIsInsertedBeforeLogoutFilter() throws Exception {

        List<Filter> filters = config.apiFilterChain(
                httpSecurity(), jwtFilter(), tokenService(), TOKEN_HEADER).getFilters();

        int jwt = indexOf(filters, JwtAuthenticationFilter.class);
        int logout = indexOf(filters, LogoutFilter.class);

        assertTrue("JwtAuthenticationFilter is not present on the /api/** chain", jwt >= 0);
        assertTrue("LogoutFilter is not present on the /api/** chain", logout >= 0);
        assertTrue("JwtAuthenticationFilter must run before LogoutFilter, otherwise the "
                        + "security context is empty when the logout handler runs and the "
                        + "session token is never revoked (position " + jwt + " vs " + logout + ")",
                jwt < logout);
    }

    /**
     * {@link ApiTokenAuthenticationFilter} refuses to authenticate unless
     * {@link HmacVerifyingFilter} has already marked the signature valid, so the order of
     * the two on the server-to-server chain is load-bearing rather than cosmetic.
     */
    @Test
    public void hmacFilterIsInsertedBeforeApiTokenFilter() throws Exception {

        List<Filter> filters = config.s2sFilterChain(
                httpSecurity(), new HmacVerifyingFilter(), apiTokenFilter()).getFilters();

        int hmac = indexOf(filters, HmacVerifyingFilter.class);
        int apiToken = indexOf(filters, ApiTokenAuthenticationFilter.class);

        assertTrue("HmacVerifyingFilter is not present on the /api/s2s/** chain", hmac >= 0);
        assertTrue("ApiTokenAuthenticationFilter is not present on the /api/s2s/** chain",
                apiToken >= 0);
        assertTrue("HmacVerifyingFilter must run before ApiTokenAuthenticationFilter, which "
                        + "authenticates only once the signature has been marked valid "
                        + "(position " + hmac + " vs " + apiToken + ")",
                hmac < apiToken);
    }

    /**
     * All three authentication filters are {@code Filter} beans, and Spring Boot registers
     * any such bean with the servlet container, where it runs on every request ahead of all
     * four chains and outside the scope it is written for. The disabled registration beans
     * are what suppress that. Deleting any one of them reintroduces the fault silently:
     * {@code OncePerRequestFilter} makes the in-chain instance a no-op afterwards, so
     * nothing misbehaves visibly.
     */
    @Test
    public void authenticationFiltersAreNotRegisteredWithTheServletContainer() {

        assertRegistrationDisabled("apiToken", config.apiTokenFilterRegistration(apiTokenFilter()));
        assertRegistrationDisabled("jwt", config.jwtFilterRegistration(jwtFilter()));
        assertRegistrationDisabled("hmac", config.hmacFilterRegistration(new HmacVerifyingFilter()));
    }

    /**
     * Chain precedence. The first chain whose matcher accepts a request wins, so these
     * four values decide, for example, whether {@code /api/s2s/**} is handled by the
     * server-to-server chain or swallowed by the broader {@code /api/**} one.
     */
    @Test
    public void chainsAreOrderedServerToServerThenPublicThenApiThenSaml() throws Exception {

        assertEquals("s2s must be the first chain", 1, orderOf("s2sFilterChain"));
        assertEquals("public endpoints must precede the authenticated /api/** chain",
                2, orderOf("publicFilterChain"));
        assertEquals("/api/** must precede the SAML catch-all", 3, orderOf("apiFilterChain"));
        assertEquals("SAML is the catch-all and must be last", 4, orderOf("samlFilterChain"));
    }

    /**
     * Reachable without a token is not the same as ignoring one that is presented.
     * <p>
     * {@code PublicEndpointMatchingTest.conceptSearchIsPublic} pins the first half: concept
     * search must answer an anonymous caller. But the {@code @Order(2)} chain that makes it
     * public installs no authentication filter at all, so a token sent <em>with</em> a
     * concept request is never read either, and the chain wins over {@code /api/**} before
     * the JWT filter is ever reached.
     * <p>
     * That is not cosmetic. {@code VocabularyConversionServiceImpl#getUnavailableVocabularyConversions}
     * branches on {@code userService.currentUserExists()}, which is now permanently false
     * here, so the anonymous set of restricted vocabularies is applied to everybody: a user
     * holding an approved CPT4 licence still has those concepts stripped from search results,
     * and {@code GET /api/v1/concepts/{id}} on one of them raises {@code LicenseException}.
     * The pac4j configuration this replaced ran {@code "HeaderClient,AnonymousClient"} —
     * authentication optional, not authentication absent — and that is the property being
     * asserted here.
     */
    @Test
    public void conceptRequestsAreAnonymousFriendlyButStillReadAPresentedToken() throws Exception {

        SecurityFilterChain chain = firstChainMatching("/api/v1/concepts");

        assertTrue("no chain claims /api/v1/concepts", chain != null);
        assertTrue("The chain serving /api/v1/concepts installs no JwtAuthenticationFilter, so a "
                        + "token presented with the request is ignored and every caller is "
                        + "treated as anonymous — licensed users lose access to the concepts "
                        + "their licence grants",
                indexOf(chain.getFilters(), JwtAuthenticationFilter.class) >= 0);
    }

    /**
     * The chains in declared precedence, first match wins — the same resolution
     * {@code FilterChainProxy} performs at runtime. The SAML catch-all is omitted: it needs a
     * relying party registration, and anything reaching it has already failed the assertions
     * that use this.
     */
    private SecurityFilterChain firstChainMatching(String uri) throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        ServletRequestPathUtils.parseAndCache(request);

        List<SecurityFilterChain> chains = List.of(
                config.s2sFilterChain(httpSecurity(), new HmacVerifyingFilter(), apiTokenFilter()),
                config.publicFilterChain(httpSecurity(), jwtFilter()),
                config.apiFilterChain(httpSecurity(), jwtFilter(), tokenService(), TOKEN_HEADER));

        for (SecurityFilterChain chain : chains) {
            if (chain.matches(request)) {
                return chain;
            }
        }
        return null;
    }

    private static void assertRegistrationDisabled(String name,
                                                   FilterRegistrationBean<?> registration) {

        assertFalse("The " + name + " filter must not be registered with the servlet "
                + "container; it is wired into its chain explicitly", registration.isEnabled());
    }

    private static int indexOf(List<Filter> filters, Class<? extends Filter> type) {

        for (int i = 0; i < filters.size(); i++) {
            if (type.isInstance(filters.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int orderOf(String beanMethod) {

        for (Method method : WebSecurityConfig.class.getDeclaredMethods()) {
            if (method.getName().equals(beanMethod)) {
                Order order = method.getAnnotation(Order.class);
                assertTrue("No @Order on " + beanMethod, order != null);
                return order.value();
            }
        }
        throw new AssertionError("No such bean method: " + beanMethod);
    }

    /**
     * A fresh builder per chain — {@link HttpSecurity} can only be built once. The context
     * carries no beans; the configurers used here fall back to their defaults when they
     * find none.
     */
    private static HttpSecurity httpSecurity() {

        ObjectPostProcessor<Object> postProcessor = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };

        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();

        Map<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, context);
        // securityMatcher(String...) resolves this from the context and falls back to the
        // shared object; with neither present it dereferences null.
        sharedObjects.put(PathPatternRequestMatcher.Builder.class,
                PathPatternRequestMatcher.withDefaults());

        return new HttpSecurity(postProcessor,
                new AuthenticationManagerBuilder(postProcessor), sharedObjects);
    }

    // The filters are located, never invoked, so their collaborators are not needed.

    private static JwtTokenService tokenService() {

        return new JwtTokenService(SALT, 604800L, null);
    }

    private static JwtAuthenticationFilter jwtFilter() {

        return new JwtAuthenticationFilter(tokenService(), null, TOKEN_HEADER);
    }

    private static ApiTokenAuthenticationFilter apiTokenFilter() {

        return new ApiTokenAuthenticationFilter(null);
    }
}
