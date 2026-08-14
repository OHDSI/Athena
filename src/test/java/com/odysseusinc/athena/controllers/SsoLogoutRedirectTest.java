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
 * Created: August 2, 2026
 *
 */

package com.odysseusinc.athena.controllers;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Shape of the logout URL sent to the identity provider.
 * <p>
 * The provider only returns the popup to this application when a {@code service} parameter is
 * present and it is configured to follow service redirects. Without it the popup is left on
 * the provider's own page, and the user cannot sign in as somebody else.
 * <p>
 * The value has to be form-encoded, not merely URI-encoded: a provider matches it against its
 * registered services as an opaque string, so a {@code :} or {@code /} left intact does not
 * match. That is the reason the controller does not build this with
 * {@code UriComponentsBuilder}, and it is what these assertions pin down.
 * <p>
 * Addresses here are examples. Nothing in this test — or in the profiles it stands in for —
 * names a real deployment.
 * <p>
 * JUnit 4 on purpose.
 */
public class SsoLogoutRedirectTest {

    private static SSOController controller(String logoutUri) {

        SSOController controller = new SSOController();
        ReflectionTestUtils.setField(controller, "sloUri", logoutUri);
        return controller;
    }

    @Test
    public void appendsAnEncodedServicePointingAtTheLandingPage() {

        String location = controller("https://idp.example.org/logout")
                .logoutRedirectUrl("https://athena.example.org/auth/logged-out");

        assertEquals("https://idp.example.org/logout"
                        + "?service=https%3A%2F%2Fathena.example.org%2Fauth%2Flogged-out",
                location);
    }

    /** A configured logout URL may already carry parameters; the service is added to them. */
    @Test
    public void keepsAnExistingQueryStringIntact() {

        String location = controller("https://idp.example.org/logout?realm=athena")
                .logoutRedirectUrl("https://athena.example.org/auth/logged-out");

        assertTrue(location, location.startsWith("https://idp.example.org/logout?realm=athena&"));
        assertTrue(location, location.contains(
                "service=https%3A%2F%2Fathena.example.org%2Fauth%2Flogged-out"));
    }

    /**
     * The separators inside the service value must survive as escapes. If they do not, the
     * provider sees a different string than the one it has registered and silently declines
     * to redirect — which looks like the popup simply hanging on the provider's page.
     */
    @Test
    public void doesNotLeaveSeparatorsUnescapedInTheServiceValue() {

        String location = controller("https://idp.example.org/logout")
                .logoutRedirectUrl("https://athena.example.org/auth/logged-out");
        String service = location.substring(location.indexOf("service=") + "service=".length());

        assertEquals("https%3A%2F%2Fathena.example.org%2Fauth%2Flogged-out", service);
    }

    @Test
    public void landingPathIsStable() {

        assertEquals("/auth/logged-out", SSOController.LOGGED_OUT_PATH);
    }
}
