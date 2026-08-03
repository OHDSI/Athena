/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.config;

import com.odysseusinc.athena.security.AthenaAuthentication;
import com.odysseusinc.athena.security.apitoken.ApiTokenAuthenticationFilter;
import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import com.odysseusinc.athena.security.jwt.JwtAuthenticationFilter;
import com.odysseusinc.athena.security.jwt.JwtTokenService;
import com.odysseusinc.athena.security.saml.SamlAuthenticationSuccessHandler;
import com.odysseusinc.athena.security.saml.SamlRelyingPartyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Application security. Replaces the pac4j-based configuration.
 * <p>
 * Four chains, in order. Each declares its own request matcher; the first match wins.
 * <ol>
 *   <li><b>{@code /api/s2s/**}</b> — server-to-server, HMAC-signed + API token.</li>
 *   <li><b>Public endpoints</b> — registration, password reset, bundle download by
 *       UUID, build number, static assets.</li>
 *   <li><b>{@code /api/**}</b> — JWT session token; authentication required.</li>
 *   <li><b>SAML + everything else</b> — SSO login, logout, view controllers.</li>
 * </ol>
 */
@Slf4j
@Configuration
@EnableWebSecurity
// prePostEnabled is explicitly false: it defaults to true on @EnableMethodSecurity,
// whereas the @EnableGlobalMethodSecurity(securedEnabled = true) this replaced defaulted
// it to false. Leaving the default would silently activate @PreAuthorize/@PostAuthorize
// processing that was previously inert. Nothing in the codebase uses those annotations
// today, so this only pins the scope rather than changing behaviour.
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = false)
public class WebSecurityConfig {

    /**
     * Endpoints that must work without a session. Previously in
     * {@code WebSecurity.ignoring()}.
     * <p>
     * These are matched with {@link PathPatternRequestMatcher} — Spring Security 7 removed
     * {@code AntPathRequestMatcher}. Patterns are written as proper subtree matchers,
     * because the original {@code "/api/v1/users**"} silently failed to cover
     * {@code /api/v1/users/anything} even under Ant matching.
     * <p>
     * One semantic difference is worth knowing: under {@code PathPattern}, {@code **} does
     * not match across segments mid-pattern, so {@code "/**.js"} matches {@code /app.x.js}
     * but not {@code /static/js/app.js}. That is harmless here — nested static assets fall
     * through to the catch-all chain, which permits them anyway — but it means this list
     * is narrower than the Ant original.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/users/remind-password",
            "/api/v1/users/reset-password",
            "/api/v1/users/professional-types",
            "/api/v1/users/countries",
            "/api/v1/users/provinces",
            "/api/v1/vocabularies/licenses/accept/mail",  // token-bound link from email
            // Vocabulary release version, read by the front end's About dialog before a user
            // signs in. Spelled the way the controller maps it — the camelCase form that was
            // listed here matches no handler.
            "/api/v1/vocabularies/release-version",
            "/api/v1/vocabularies/zip/**",
            "/api/v1/build-number",
            "/api/v1/concepts",
            "/api/v1/concepts/**"
    };

    private static final String[] PUBLIC_RESOURCES = {
            "/", "/error", "/index.html", "/**.js", "/fonts/**", "/icons/**", "/img/**",
            "/app.*.js", "/webjars/**", "/swagger-ui.html", "/swagger-resources/**",
            "/v3/api-docs/**", "/auth/saml-metadata", "/auth/slo", "/auth/logged-out"
    };


    /**
     * Spring Security 7 removed {@code AntPathRequestMatcher}; {@link
     * PathPatternRequestMatcher} is the replacement. Unlike the MVC
     * {@code PathPatternParser}, this accepts the {@code **} subtree patterns used here.
     */
    private static RequestMatcher matcher(String pattern) {

        return PathPatternRequestMatcher.withDefaults().matcher(pattern);
    }

    private static RequestMatcher matcher(HttpMethod method, String pattern) {

        return PathPatternRequestMatcher.withDefaults().matcher(method, pattern);
    }

    /** Every request chain 2 should handle: POST-only registration, then the flat lists. */
    private static List<RequestMatcher> publicMatchers() {

        List<RequestMatcher> matchers = new ArrayList<>();
        matchers.add(matcher(HttpMethod.POST, "/api/v1/users"));
        for (String pattern : PUBLIC_ENDPOINTS) {
            matchers.add(matcher(pattern));
        }
        for (String pattern : PUBLIC_RESOURCES) {
            matchers.add(matcher(pattern));
        }
        return matchers;
    }

    /**
     * Both authentication filters are {@code @Component}s so they can be injected into
     * the chains below, and Spring Boot auto-registers <em>any</em> {@code Filter} bean
     * with the servlet container — which would run them on every request, outside the
     * chain that is supposed to scope them. An API token would then authenticate on the
     * {@code /api/**} chain, and the JWT filter would run on public endpoints where
     * chain 2 deliberately has no authentication.
     * <p>
     * Registering them with {@code setEnabled(false)} suppresses only the container
     * registration; the explicit {@code addFilterBefore}/{@code addFilterAfter} wiring
     * in the chains is untouched, so each filter runs exactly where it is declared.
     */
    @Bean
    public FilterRegistrationBean<ApiTokenAuthenticationFilter> apiTokenFilterRegistration(
            ApiTokenAuthenticationFilter filter) {

        FilterRegistrationBean<ApiTokenAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Server-to-server. {@link HmacVerifyingFilter} must run before
     * {@link ApiTokenAuthenticationFilter}, because the latter refuses to authenticate
     * unless the former has marked the signature valid.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain s2sFilterChain(HttpSecurity http,
                                              HmacVerifyingFilter hmacVerifyingFilter,
                                              ApiTokenAuthenticationFilter apiTokenFilter) throws Exception {

        return http
                .securityMatcher("/api/s2s/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(hmacVerifyingFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(apiTokenFilter, HmacVerifyingFilter.class)
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .build();
    }

    /**
     * Public endpoints and static assets. No authentication filters, mirroring the
     * previous {@code WebSecurity.ignoring()} behaviour.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {

        return http
                // Registration is public only as a POST. The path carries no GET handler,
                // so opening the whole path would expose surface for no reason.
                .securityMatcher(new OrRequestMatcher(publicMatchers()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .build();
    }

    /**
     * Everything else under {@code /api/**} needs a valid session token. Method-level
     * {@code @Secured} still applies on top for admin endpoints.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                              JwtAuthenticationFilter jwtFilter,
                                              JwtTokenService tokenService) throws Exception {

        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutRequestMatcher(matcher("/api/v1/users/logout"))
                        .addLogoutHandler(revokeTokenHandler(tokenService))
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(200)))
                .build();
    }

    /**
     * SAML SSO and the remaining UI routes.
     * <p>
     * The assertion consumer service stays on the path the identity provider already has
     * registered ({@code /auth/callback}) — see {@link SamlRelyingPartyConfig}.
     * {@code /auth/sso} remains the login entry point; requiring authentication there is what
     * triggers the SAML entry point, so the externally visible URL is unchanged.
     * <p>
     * {@code loginPage} has to be set explicitly. Spring Security only auto-redirects to a
     * single identity provider when the registration repository is {@link Iterable}, and this
     * one deliberately is not — see
     * {@link SamlRelyingPartyConfig#AUTHENTICATION_REQUEST_PATH}. Without it the default
     * {@code /login} chooser renders with an empty provider list and the popup never leaves
     * the application.
     */
    @Bean
    @Order(4)
    public SecurityFilterChain samlFilterChain(HttpSecurity http,
                                               SamlAuthenticationSuccessHandler successHandler,
                                               RelyingPartyRegistrationRepository registrations)
            throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .saml2Login(saml2 -> saml2
                        .loginProcessingUrl(SamlRelyingPartyConfig.ASSERTION_CONSUMER_SERVICE_PATH)
                        .loginPage(SamlRelyingPartyConfig.AUTHENTICATION_REQUEST_PATH)
                        // Spring Security insists on {registrationId} in the assertion
                        // consumer service path unless a converter supplies the
                        // registration itself. The IdP already has /auth/callback
                        // registered with no such variable, so pin the single
                        // registration here rather than change the endpoint.
                        .authenticationConverter(fixedRegistrationConverter(registrations))
                        .successHandler(successHandler))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/sso").authenticated()
                        .anyRequest().permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .build();
    }

    /**
     * Resolves the one and only relying party regardless of the request path, so the
     * assertion consumer service can stay on the registered {@code /auth/callback}
     * instead of Spring Security's {@code /login/saml2/sso/{registrationId}}.
     * <p>
     * Delegates to {@link DefaultRelyingPartyRegistrationResolver} rather than reading
     * the repository directly, because the delegate is what expands the
     * {@code {baseUrl}} placeholder in the registration's assertion consumer service
     * location — that expanded value goes into the {@code AssertionConsumerServiceURL}
     * of the outgoing {@code AuthnRequest}, and IdPs validate it.
     */
    private Saml2AuthenticationTokenConverter fixedRegistrationConverter(
            RelyingPartyRegistrationRepository registrations) {

        RelyingPartyRegistrationResolver delegate =
                new DefaultRelyingPartyRegistrationResolver(registrations);

        return new Saml2AuthenticationTokenConverter(
                (RelyingPartyRegistrationResolver) (request, ignoredRegistrationId) ->
                        delegate.resolve(request, SamlRelyingPartyConfig.REGISTRATION_ID));
    }

    /**
     * Adds the presented JWT to the revoked-token store so it cannot be replayed after
     * logout. Replaces {@code CustomLogoutLogic}, which reached into pac4j's session
     * profile map to find the token.
     */
    private org.springframework.security.web.authentication.logout.LogoutHandler revokeTokenHandler(
            JwtTokenService tokenService) {

        return (request, response, authentication) -> {
            if (authentication instanceof AthenaAuthentication) {
                ((AthenaAuthentication) authentication).getToken().ifPresent(token -> {
                    tokenService.revoke(token);
                    log.debug("Revoked the session token of user [{}]", authentication.getName());
                });
            }
        };
    }
}
