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

package com.odysseusinc.athena.security;

import com.odysseusinc.athena.model.security.AthenaRole;
import com.odysseusinc.athena.model.security.AthenaUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The authenticated principal for every Athena request, whatever the entry point
 * (SAML login, JWT header, or an S2S API token).
 * <p>
 * Replaces the pac4j {@code AthenaProfile} / {@code Pac4jAuthenticationToken}
 * pairing. Two things callers rely on:
 * <ul>
 *   <li>{@link #getPrincipal()} is the {@link AthenaUser} itself, so
 *       {@code UserService.getUser(Principal)} no longer has to unwrap nested
 *       {@code Optional}s out of a pac4j token.</li>
 *   <li>{@link #getToken()} carries the raw JWT when the request was authenticated
 *       by one, so logout can add it to the revoked-token store. It is empty for
 *       API-token and SAML-session authentication, which have nothing to revoke
 *       this way.</li>
 * </ul>
 * Authorities come from {@link AthenaUser#getRoles()} and keep the {@code ROLE_}
 * prefix already stored in the database, so {@code @Secured("ROLE_ADMIN")}
 * continues to work unchanged.
 */
public class AthenaAuthentication extends AbstractAuthenticationToken {

    private final AthenaUser user;
    private final String token;

    /**
     * @param user never null — an authenticated request always has a resolved user.
     *             Callers must not construct this to represent "no user"; leave the
     *             security context empty instead.
     */
    private AthenaAuthentication(AthenaUser user, String token,
                                 Collection<? extends GrantedAuthority> authorities) {

        super(authorities);
        this.user = Objects.requireNonNull(user, "an authenticated user is required");
        this.token = token;
        setAuthenticated(true);
    }

    /**
     * Authenticated by a JWT presented in the {@code athena.token.header} header.
     */
    public static AthenaAuthentication withToken(AthenaUser user, String token) {

        return new AthenaAuthentication(user, token, authoritiesOf(user));
    }

    /**
     * Authenticated without a revocable bearer token — an S2S API token or a
     * freshly completed SAML login.
     */
    public static AthenaAuthentication withoutToken(AthenaUser user) {

        return new AthenaAuthentication(user, null, authoritiesOf(user));
    }

    /** {@code user} is non-null by the contract above; only its roles may be absent. */
    private static Collection<GrantedAuthority> authoritiesOf(AthenaUser user) {

        List<AthenaRole> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(AthenaRole::getName)
                .filter(Objects::nonNull)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public AthenaUser getPrincipal() {

        return user;
    }

    @Override
    public Object getCredentials() {

        return token;
    }

    /**
     * The raw JWT this request was authenticated with, if any.
     */
    public Optional<String> getToken() {

        return Optional.ofNullable(token);
    }

    /**
     * The username, so that {@code Principal.getName()} keeps behaving as before.
     */
    @Override
    public String getName() {

        return user.getUsername();
    }
}
