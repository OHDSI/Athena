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
 * Created: August 3, 2026
 *
 */

package com.odysseusinc.athena.config;

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.security.AthenaAuthentication;
import com.odysseusinc.athena.security.jwt.JwtTokenService;
import com.odysseusinc.athena.service.security.RevokedTokenStore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Logging out must make the presented token unusable.
 * <p>
 * The regression this guards is specific and was silent. The handler used to take the token
 * only from the security context, and the authentication filter was inserted before
 * {@code BasicAuthenticationFilter} — which sits far later in the chain than
 * {@code LogoutFilter}. On a {@code STATELESS} chain that left the context empty for the whole
 * logout request, so nothing was ever revoked while logout still answered 200. The token
 * stayed valid for its full lifetime.
 * <p>
 * These tests therefore drive the handler with an <em>empty</em> context on purpose: that is
 * the state the bug produced, and revocation has to survive it.
 * <p>
 * JUnit 4 on purpose.
 */
public class LogoutRevocationTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String HEADER = "Athena-Auth-Token";

    private InMemoryRevokedTokenStore revoked;
    private JwtTokenService service;
    private LogoutHandler handler;

    @Before
    public void setUp() {

        revoked = new InMemoryRevokedTokenStore();
        service = new JwtTokenService(SECRET, 3600, revoked);
        handler = WebSecurityConfig.revokeTokenHandler(service, HEADER);
    }

    private static AthenaUser user() {

        AthenaUser user = new AthenaUser();
        user.setUsername("jane");
        user.setOrigin("saml");
        return user;
    }

    private MockHttpServletRequest logoutRequest(String tokenHeader) {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/logout");
        if (tokenHeader != null) {
            request.addHeader(HEADER, tokenHeader);
        }
        return request;
    }

    /** The path that used to work, and must keep working. */
    @Test
    public void revokesTheTokenCarriedByAnAuthenticatedContext() {

        String token = service.issue(user());
        handler.logout(logoutRequest(null), new MockHttpServletResponse(),
                AthenaAuthentication.withToken(user(), token));

        assertTrue("the token must be unusable after logout", revoked.contains(token));
        assertFalse(service.validate(token).isPresent());
    }

    /** The regression itself: no context, token only on the request. */
    @Test
    public void revokesTheTokenWhenTheContextIsEmpty() {

        String token = service.issue(user());
        handler.logout(logoutRequest(token), new MockHttpServletResponse(), null);

        assertTrue("revocation must not depend on where the authentication filter sits",
                revoked.contains(token));
        assertFalse(service.validate(token).isPresent());
    }

    /**
     * The header is unauthenticated input and the revocation store is unbounded, so anything
     * that is not a genuine token must not be written to it.
     */
    @Test
    public void ignoresAHeaderThatIsNotAValidToken() {

        handler.logout(logoutRequest("not-a-token"), new MockHttpServletResponse(), null);
        assertFalse(revoked.contains("not-a-token"));
        assertTrue(revoked.isEmpty());
    }

    /** A token signed with somebody else's key is not a token this application issued. */
    @Test
    public void ignoresAHeaderSignedWithAnotherSecret() {

        String foreign = new JwtTokenService("fedcba9876543210fedcba9876543210", 3600,
                new InMemoryRevokedTokenStore()).issue(user());

        handler.logout(logoutRequest(foreign), new MockHttpServletResponse(), null);

        assertTrue(revoked.isEmpty());
    }

    @Test
    public void doesNothingWhenNoTokenIsPresentedAtAll() {

        handler.logout(logoutRequest(null), new MockHttpServletResponse(), null);
        assertTrue(revoked.isEmpty());
    }

    private static final class InMemoryRevokedTokenStore implements RevokedTokenStore {

        private final Set<String> tokens = new HashSet<>();

        boolean isEmpty() {

            return tokens.isEmpty();
        }

        @Override
        public boolean contains(String token) {

            return tokens.contains(token);
        }

        @Override
        public void invalidate(String token) {

            if (token != null) {
                tokens.add(token);
            }
        }
    }
}
