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
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.config;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Which paths reach the {@code @Order(2)} public chain decides which
 * endpoints are reachable without authentication, and that is expressed only as two string
 * arrays — nothing else checks them.
 * <p>
 * The old configuration had a {@code /api/v1/concepts**}
 * {@code permitAll} sat behind the authenticated {@code /api/**} chain and so never applied.
 * The rewrite moved those patterns into {@code PUBLIC_ENDPOINTS}, which the {@code @Order(2)}
 * chain matches ahead of the {@code @Order(3)} one. This asserts that is actually true rather
 * than inferring it from the ordering.
 * <p>
 * The negative cases matter more than the positive ones: they are regression guards for
 * (user enumeration) and for the POST-only registration endpoint.
 * <p>
 * JUnit 4 on purpose.
 */
public class PublicEndpointMatchingTest {

    @SuppressWarnings("unchecked")
    private static final RequestMatcher PUBLIC = new OrRequestMatcher(
            (List<RequestMatcher>) ReflectionTestUtils.invokeMethod(
                    WebSecurityConfig.class, "publicMatchers"));

    private static boolean isPublic(HttpMethod method, String uri) {

        MockHttpServletRequest request = new MockHttpServletRequest(method.name(), uri);
        request.setRequestURI(uri);
        ServletRequestPathUtils.parseAndCache(request);
        return PUBLIC.matches(request);
    }

    private static boolean isPublic(String uri) {

        return isPublic(HttpMethod.GET, uri);
    }

    /** The concept search really is reachable unauthenticated now. */
    @Test
    public void conceptSearchIsPublic() {

        assertTrue(isPublic("/api/v1/concepts"));
        assertTrue(isPublic("/api/v1/concepts/123"));
    }

    @Test
    public void theOtherAdvertisedPublicEndpointsMatch() {

        assertTrue(isPublic("/api/v1/users/remind-password"));
        assertTrue(isPublic("/api/v1/vocabularies/licenses/accept/mail"));
        assertTrue(isPublic("/api/v1/build-number"));
        assertTrue(isPublic("/api/v1/vocabularies/zip/some-uuid"));
    }

    /**
     * The About dialog reads this before anyone signs in, so the entry has to match the path
     * the controller actually maps — {@code @GetMapping("/release-version")} on
     * {@code AbstractVocabularyController}, under the {@code /api/v1/vocabularies} base.
     */
    @Test
    public void vocabularyReleaseVersionIsPublicUnderItsRealPath() {

        assertTrue(isPublic("/api/v1/vocabularies/release-version"));
        assertFalse(isPublic("/api/v1/vocabularies/releaseVersion"));
    }

    /**
     * The same handler is inherited by the server-to-server controller, which sits behind
     * signature verification and must not be opened up by the entry above.
     */
    @Test
    public void theServerToServerReleaseVersionStaysProtected() {

        assertFalse(isPublic("/api/s2s/vocabularies/release-version"));
    }

    /** Both ends of the logout popup are reached without a session by definition. */
    @Test
    public void theLogoutPopupPathsArePublic() {

        assertTrue(isPublic("/auth/slo"));
        assertTrue(isPublic("/auth/logged-out"));
    }

    /** user enumeration must stay behind authentication. */
    @Test
    public void userSuggestIsNotPublic() {

        assertFalse(isPublic("/api/v1/users/suggest"));
    }

    /** Registration is public as a POST only — the path has no GET handler. */
    @Test
    public void registrationIsPublicOnlyAsAPost() {

        assertTrue(isPublic(HttpMethod.POST, "/api/v1/users"));
        assertFalse(isPublic(HttpMethod.GET, "/api/v1/users"));
    }

    /** The authenticated API surface must not leak into the public chain. */
    @Test
    public void theAuthenticatedApiIsNotPublic() {

        assertFalse(isPublic("/api/v1/vocabularies"));
        assertFalse(isPublic("/api/v1/vocabularies/downloads/1/share"));
        assertFalse(isPublic("/api/v1/vocabularies/check/1"));
        assertFalse(isPublic("/api/v1/admin/statistics"));
    }

    /** The S2S surface is handled by its own chain and must not be permitted here. */
    @Test
    public void theS2sApiIsNotPublic() {

        assertFalse(isPublic("/api/s2s/vocabularies"));
    }
}
