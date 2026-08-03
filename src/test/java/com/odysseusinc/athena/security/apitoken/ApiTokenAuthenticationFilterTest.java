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
import com.odysseusinc.athena.security.hmac.HmacVerifyingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The HMAC gate is the whole point of this filter: an API token on its own must never
 * authenticate anyone. Nothing tested that before, which is how the broken
 * nonce check in {@link HmacVerifyingFilter} went unnoticed.
 * <p>
 * JUnit 4 on purpose.
 */
public class ApiTokenAuthenticationFilterTest {

    private static final String TOKEN = "a-valid-api-token";

    private ApiTokenAuthenticationFilter filter;

    @Before
    public void setUp() {

        AthenaUser user = new AthenaUser();
        user.setId(7L);
        user.setUsername("service-account");

        AthenaToken token = new AthenaToken();
        token.setUser(user);
        token.setValue(TOKEN);

        AthenaTokenRepository repository = mock(AthenaTokenRepository.class);
        when(repository.findByValue(anyString())).thenReturn(Optional.empty());
        when(repository.findByValue(TOKEN)).thenReturn(Optional.of(token));

        filter = new ApiTokenAuthenticationFilter(repository);
    }

    @After
    public void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    public void authenticatesWhenTheHmacIsValidAndTheTokenIsKnown() throws Exception {

        Authentication authentication = run(request(true, "Bearer " + TOKEN));

        assertNotNull("a correctly signed request with a known token must authenticate",
                authentication);
        assertEquals("service-account", authentication.getName());
    }

    /** The security-critical case. */
    @Test
    public void refusesAValidTokenWhenTheHmacIsNotValid() throws Exception {

        assertNull("a token must never authenticate without a verified HMAC",
                run(request(false, "Bearer " + TOKEN)));
    }

    @Test
    public void refusesAValidTokenWhenTheHmacAttributeIsAbsentEntirely() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ApiTokenAuthenticationFilter.AUTH_HEADER, "Bearer " + TOKEN);

        assertNull("a missing HMAC attribute must be treated as untrusted, not as valid",
                run(request));
    }

    @Test
    public void refusesAnUnknownToken() throws Exception {

        assertNull(run(request(true, "Bearer not-the-right-token")));
    }

    @Test
    public void refusesAHeaderWithoutTheBearerPrefix() throws Exception {

        assertNull(run(request(true, TOKEN)));
    }

    @Test
    public void refusesAnEmptyBearerValue() throws Exception {

        assertNull(run(request(true, "Bearer   ")));
    }

    @Test
    public void leavesRequestsWithNoAuthHeaderAlone() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HmacVerifyingFilter.ATTRIBUTE_HMAC_VALID, Boolean.TRUE);

        assertNull(run(request));
    }

    /** An earlier filter in the chain wins; this one must not overwrite it. */
    @Test
    public void doesNotReplaceAnExistingAuthentication() throws Exception {

        AthenaUser other = new AthenaUser();
        other.setUsername("already-authenticated");
        SecurityContextHolder.getContext().setAuthentication(
                com.odysseusinc.athena.security.AthenaAuthentication.withoutToken(other));

        Authentication authentication = run(request(true, "Bearer " + TOKEN));

        assertEquals("already-authenticated", authentication.getName());
    }

    private MockHttpServletRequest request(boolean hmacValid, String authHeader) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HmacVerifyingFilter.ATTRIBUTE_HMAC_VALID, hmacValid);
        request.addHeader(ApiTokenAuthenticationFilter.AUTH_HEADER, authHeader);
        return request;
    }

    private Authentication run(MockHttpServletRequest request) throws Exception {

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
