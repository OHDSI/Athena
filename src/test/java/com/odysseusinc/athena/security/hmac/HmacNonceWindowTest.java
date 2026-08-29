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

package com.odysseusinc.athena.security.hmac;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * The replay window in {@code verifyNonce} was guarded by an '&&' of
 * "too far ahead" AND "too far behind", which is unsatisfiable — so no nonce was
 * ever rejected and a captured, correctly signed S2S request could be replayed
 * forever. Every one of the "outside the window" cases below passes against the
 * old code.
 * <p>
 * JUnit 4 on purpose.
 */
public class HmacNonceWindowTest {

    private static final Duration TOLERANCE = Duration.ofMinutes(1);

    private HmacVerifyingFilter filter;

    @Before
    public void setUp() {

        ApiClients clients = new ApiClients();
        clients.setClients(Collections.emptyMap());
        clients.setTimeTolerance(TOLERANCE);

        filter = new HmacVerifyingFilter();
        ReflectionTestUtils.setField(filter, "clients", clients);
    }

    @Test
    public void acceptsANonceAtTheCurrentTime() {

        assertAccepted(Instant.now());
    }

    @Test
    public void acceptsANonceInsideTheWindowOnEitherSide() {

        assertAccepted(Instant.now().minus(TOLERANCE).plusSeconds(5));
        assertAccepted(Instant.now().plus(TOLERANCE).minusSeconds(5));
    }

    /** The replay case: an old captured request must no longer be accepted. */
    @Test
    public void rejectsANonceFromTheDistantPast() {

        assertRejected(Instant.now().minus(Duration.ofDays(30)));
        assertRejected(Instant.now().minus(TOLERANCE).minusSeconds(10));
    }

    /** Clock skew or a client pre-generating nonces must not be accepted either. */
    @Test
    public void rejectsANonceFromTheDistantFuture() {

        assertRejected(Instant.now().plus(Duration.ofDays(30)));
        assertRejected(Instant.now().plus(TOLERANCE).plusSeconds(10));
    }

    @Test
    public void rejectsAMissingNonce() {

        try {
            verify(new MockHttpServletRequest());
            fail("a request with no nonce header must be rejected");
        } catch (BadCredentialsException expected) {
            assertEquals(true, expected.getMessage().contains(HmacVerifyingFilter.HEADER_NONCE));
        }
    }

    @Test
    public void rejectsAnUnparseableNonce() {

        for (String bad : new String[]{"", "not-a-timestamp", "1751234567", "2026-07-29"}) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HmacVerifyingFilter.HEADER_NONCE, bad);
            try {
                verify(request);
                fail("nonce [" + bad + "] is not an ISO-8601 instant and must be rejected");
            } catch (BadCredentialsException expected) {
                // wanted
            }
        }
    }

    private void assertAccepted(Instant nonce) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HmacVerifyingFilter.HEADER_NONCE, nonce.toString());
        assertEquals("nonce " + nonce + " is inside the tolerance and must be accepted",
                nonce.toString(), verify(request));
    }

    private void assertRejected(Instant nonce) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HmacVerifyingFilter.HEADER_NONCE, nonce.toString());
        try {
            verify(request);
            fail("nonce " + nonce + " is outside the tolerance and must be rejected");
        } catch (BadCredentialsException expected) {
            // wanted
        }
    }

    private String verify(MockHttpServletRequest request) {

        return (String) ReflectionTestUtils.invokeMethod(filter, "verifyNonce", request);
    }
}
