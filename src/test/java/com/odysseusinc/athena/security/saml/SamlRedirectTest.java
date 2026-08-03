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

package com.odysseusinc.athena.security.saml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Where a completed SSO login puts the token.
 * <p>
 * It used to go in the query string, which meant the JWT was written to this application's own
 * access log, to any proxy log in front of it, and into the {@code Referer} of requests the
 * landing page made. A fragment is never sent to a server, so none of that happens.
 * <p>
 * The counterpart is AthenaUI's {@code LoginComplete}, which reads the same key out of
 * {@code window.location.hash}. The two are asserted separately; the shared contract is the
 * key name and the fact that the token is after the {@code #}.
 * <p>
 * JUnit 4 on purpose.
 */
public class SamlRedirectTest {

    /** A realistic three-segment JWT: base64url alphabet plus dot separators. */
    private static final String JWT =
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMiLCJvcmlnaW4iOiJDQVMifQ.c2lnbmF0dXJlLXZhbHVl_-x";

    private String redirectTo(String configuredRedirectUrl) {

        return new SamlAuthenticationSuccessHandler(null, null, configuredRedirectUrl)
                .redirectUrlFor(JWT);
    }

    @Test
    public void putsTheTokenInTheFragment() {

        String location = redirectTo("http://localhost:3010/auth/complete");

        assertEquals("http://localhost:3010/auth/complete#token=" + JWT, location);
    }

    /** The regression this guards: nothing before the '#' may carry the token. */
    @Test
    public void keepsTheTokenOutOfTheQueryString() {

        String location = redirectTo("http://localhost:3010/auth/complete");
        String beforeFragment = location.substring(0, location.indexOf('#'));

        assertFalse("the token must not appear in the part sent to the server: " + beforeFragment,
                beforeFragment.contains(JWT));
        assertFalse("no token query parameter may remain", beforeFragment.contains("token="));
        assertFalse("the request line must carry no query string at all",
                beforeFragment.contains("?"));
    }

    /**
     * A JWT is base64url plus dots. None of those characters need escaping in a fragment, so
     * the value must arrive byte for byte — the front end splits on the first '=' and uses the
     * remainder verbatim.
     */
    @Test
    public void leavesTheTokenIntact() {

        String location = redirectTo("http://localhost:3010/auth/complete");
        String token = location.substring(location.indexOf("#token=") + "#token=".length());

        assertEquals(JWT, token);
        assertTrue("a JWT has three dot-separated segments", token.split("\\.").length == 3);
    }

    /** A deployment behind a path prefix still gets a well-formed URL. */
    @Test
    public void handlesARedirectUrlWithAPath() {

        String location = redirectTo("https://athena.example.org/athena/auth/complete");

        assertEquals("https://athena.example.org/athena/auth/complete#token=" + JWT, location);
    }

    /**
     * If the configured redirect already carries a query string it is preserved, and the token
     * still lands after the '#' rather than being appended to it.
     */
    @Test
    public void doesNotDisturbAnExistingQueryString() {

        String location = redirectTo("http://localhost:3010/auth/complete?next=%2Fvocabularies");

        assertTrue(location, location.startsWith("http://localhost:3010/auth/complete?next="));
        assertTrue("the token belongs after the hash", location.contains("#token=" + JWT));
        assertFalse("and not in the query",
                location.substring(0, location.indexOf('#')).contains(JWT));
    }
}
