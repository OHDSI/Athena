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

package com.odysseusinc.athena.security.jwt;

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaUserRepository;
import com.odysseusinc.athena.security.AthenaAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authenticates a request carrying a JWT in the {@code athena.token.header} header
 * (by default {@code Athena-Auth-Token}).
 * <p>
 * Replaces pac4j's {@code HeaderClient}. A missing or invalid token is <em>not</em>
 * an error here: the filter simply leaves the context unauthenticated and lets the
 * chain continue, so genuinely public endpoints still work and the authorization
 * rules — not this filter — decide what requires a user. That mirrors the previous
 * behaviour, where an anonymous client sat alongside the header client.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;
    private final AthenaUserRepository userRepository;
    private final String headerName;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   AthenaUserRepository userRepository,
                                   @Value("${athena.token.header}") String headerName) {

        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request.getHeader(headerName));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {

        if (token == null || token.isEmpty()) {
            return;
        }
        tokenService.validate(token).ifPresent(identity -> {
            AthenaUser user = userRepository.findByUsernameIgnoreCaseAndOrigin(
                    identity.getUsername(), identity.getOrigin());
            if (user == null) {
                // Valid signature, but the user has since been removed.
                log.debug("Token subject [{}] no longer resolves to a user", identity.getUsername());
                return;
            }
            SecurityContextHolder.getContext()
                    .setAuthentication(AthenaAuthentication.withToken(user, token));
        });
    }
}
