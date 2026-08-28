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
 * Created: July 29, 2026
 *
 */

package com.odysseusinc.athena.security.apitoken;

import com.odysseusinc.athena.model.security.AthenaToken;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaTokenRepository;
import com.odysseusinc.athena.security.AthenaAuthentication;
import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Authenticates server-to-server requests on {@code /api/s2s/**} using a long-lived
 * API token, but <strong>only</strong> when {@link HmacVerifyingFilter} has already
 * confirmed the request signature.
 * <p>
 * Replaces pac4j's {@code ApiTokenAuthClient} / {@code ApiTokenBearerHeaderExtractor}
 * / {@code ApiTokenAuthenticator}. The wire contract is unchanged — clients keep
 * sending:
 * <pre>
 *   X-Athena-Client-Id: &lt;client&gt;
 *   X-Athena-Auth: Bearer &lt;token&gt;
 *   X-Athena-Nonce: &lt;ISO-8601 instant&gt;
 *   X-Athena-Hmac: &lt;signature&gt;
 * </pre>
 * The HMAC gate is the important part: a token alone must never be sufficient. This
 * filter must therefore be ordered <em>after</em> {@link HmacVerifyingFilter}, which
 * sets {@link HmacVerifyingFilter#ATTRIBUTE_HMAC_VALID} on the request.
 */
@Slf4j
@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    static final String AUTH_HEADER = "X-Athena-Auth";
    /** Trailing space included: without it "Bearerabc" parses as the token "abc". */
    static final String AUTH_PREFIX = "Bearer ";

    private final AthenaTokenRepository tokenRepository;

    public ApiTokenAuthenticationFilter(AthenaTokenRepository tokenRepository) {

        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            bearerToken(request).ifPresent(this::authenticate);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token only if the request carries a verified HMAC. Returns empty
     * rather than throwing, leaving the request unauthenticated so the authorization
     * rules produce the rejection.
     */
    private Optional<String> bearerToken(HttpServletRequest request) {

        if (!Boolean.TRUE.equals(request.getAttribute(HmacVerifyingFilter.ATTRIBUTE_HMAC_VALID))) {
            return Optional.empty();
        }
        String header = request.getHeader(AUTH_HEADER);
        if (header == null) {
            return Optional.empty();
        }
        if (!header.startsWith(AUTH_PREFIX)) {
            log.debug("Header [{}] must use the [{}] prefix", AUTH_HEADER, AUTH_PREFIX);
            return Optional.empty();
        }
        String token = header.substring(AUTH_PREFIX.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private void authenticate(String token) {

        Optional<AthenaToken> found = tokenRepository.findByValue(token);
        if (!found.isPresent()) {
            log.debug("Presented API token is not recognised");
            return;
        }
        AthenaUser user = found.get().getUser();
        if (user == null) {
            // Should be impossible via the FK, but AthenaAuthentication requires a
            // resolved user; leave the request unauthenticated rather than throw.
            log.warn("API token [id={}] has no associated user", found.get().getId());
            return;
        }
        SecurityContextHolder.getContext()
                .setAuthentication(AthenaAuthentication.withoutToken(user));
    }
}
