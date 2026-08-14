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

package com.odysseusinc.athena.security.hmac;

import org.junit.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The S2S signature verification behind {@code /api/s2s/**}. This path is live in
 * production, so the surrounding findings matter:
 * <ul>
 *   <li>{@code clients} was null unless {@code api.clients.*} was
 *       configured, so a deployment missing it aborted startup with a bare NPE.</li>
 *   <li>the key factory was hardcoded to DSA while the signature algorithm
 *       is per-client configuration, so an RSA client's key could never be parsed.</li>
 * </ul>
 * JUnit 4 on purpose.
 */
public class ApiClientsTest {

    /** Startup must survive an unconfigured deployment. */
    @Test
    public void initialisesWithNoConfiguredClientsInsteadOfThrowing() {

        ApiClients clients = new ApiClients();

        clients.init();

        assertTrue("an unconfigured deployment must still start", clients.getClients().isEmpty());
    }

    /**
     * An unknown client id is a refusal, not a fault. It is reported as an
     * {@link AuthenticationException} so {@code HmacVerifyingFilter} can treat it the same
     * way as a bad signature; left uncaught it surfaced as a 500 on any request carrying
     * the header.
     */
    @Test
    public void reportsAnUnknownClientIdAsAnAuthenticationFailure() {

        ApiClients clients = new ApiClients();
        clients.init();

        try {
            clients.getSignatureVerifier("no-such-client");
            fail("an unknown client id must not resolve to a verifier");
        } catch (AuthenticationException expected) {
            assertTrue(expected.getMessage().contains("no-such-client"));
        }
    }

    /** A client configured for RSA must have its public key parsed as RSA. */
    @Test
    public void verifiesAnRsaSignedRequest() throws Exception {

        assertRoundTrip("RSA", "SHA256withRSA");
    }

    /** DSA keeps working — that is what production uses today. */
    @Test
    public void verifiesADsaSignedRequest() throws Exception {

        assertRoundTrip("DSA", "SHA256withDSA");
    }

    /**
     * The signature algorithm is {@code SHA256withECDSA} but the key algorithm is {@code EC} —
     * no standard provider registers a {@code KeyFactory} under {@code ECDSA}. Deriving the key
     * algorithm by chopping off everything before "with" gets this one wrong, and it fails as a
     * NoSuchAlgorithmException at verification time.
     */
    @Test
    public void verifiesAnEcSignedRequest() throws Exception {

        assertRoundTrip("EC", "SHA256withECDSA");
    }

    @Test
    public void mapsSignatureAlgorithmsToKeyAlgorithms() {

        assertEquals("RSA", ApiClients.keyAlgorithmOf("SHA256withRSA"));
        assertEquals("DSA", ApiClients.keyAlgorithmOf("SHA1withDSA"));
        assertEquals("EC", ApiClients.keyAlgorithmOf("SHA256withECDSA"));
        assertEquals("EC", ApiClients.keyAlgorithmOf("SHA256withECDSAinP1363Format"));
        assertEquals("DSA", ApiClients.keyAlgorithmOf("SHA1withDSAinP1363Format"));
        // Names that are their own key algorithm and contain no "with".
        assertEquals("Ed25519", ApiClients.keyAlgorithmOf("Ed25519"));
        assertEquals("RSASSA-PSS", ApiClients.keyAlgorithmOf("RSASSA-PSS"));
        // Absent configuration keeps the historical default.
        assertEquals("DSA", ApiClients.keyAlgorithmOf(null));
    }

    /**
     * An unrecognised algorithm is rejected where it is configured, rather than silently
     * treated as DSA and surfacing later as an opaque InvalidKeySpecException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsAnAlgorithmItCannotMap() {

        ApiClients.keyAlgorithmOf("not-a-signature-algorithm");
    }

    /**
     * A bare key algorithm name is valid configuration, not a mapping failure.
     * <p>
     * {@code keyAlgorithmOf} recognises a name only if it contains {@code with} or appears in
     * the self-named list, and {@code DSA} does neither — so it lands on the
     * {@code IllegalArgumentException} branch. That is a regression rather than a tightening:
     * the key factory used to be hardcoded to {@code DSA}, so
     * {@code api.clients.<id>.algorithm=DSA} parsed the public key and verified signatures.
     * The method's own documentation still claims it falls "back to DSA so existing DSA-based
     * clients are unaffected", which holds for a null value but not for the literal name.
     * <p>
     * It fails as a 500 rather than a refusal, because {@code IllegalArgumentException} is not
     * an {@code AuthenticationException} and so escapes {@code HmacVerifyingFilter.verify}.
     */
    @Test
    public void mapsBareKeyAlgorithmNamesToThemselves() {

        assertEquals("DSA", ApiClients.keyAlgorithmOf("DSA"));
        assertEquals("RSA", ApiClients.keyAlgorithmOf("RSA"));
    }

    /**
     * A client configured with an algorithm but no {@code public-key} must be refused, not
     * blow up.
     * <p>
     * {@code init()} only fabricates a transient pair when {@code debugSignatures} is on, which
     * it is not by default, so {@code getPublicKey()} stays null and {@code parseKey} reaches
     * {@code Base64.getDecoder().decode(null)}. The resulting {@code NullPointerException} is
     * not an {@code AuthenticationException}, so — exactly like an unknown client id before it
     * was fixed — it escapes the filter as a 500 with a stack trace on every
     * {@code /api/s2s/**} call from that client. This is the state the startup warning already
     * describes as "requests cannot be authenticated"; it should refuse them, not fault.
     */
    @Test
    public void reportsAClientWithNoPublicKeyAsAnAuthenticationFailure() {

        ApiClients.ApiClient client = new ApiClients.ApiClient();
        client.setAlgorithm("SHA256withRSA");

        ApiClients clients = new ApiClients();
        ReflectionTestUtils.setField(clients, "clients", Collections.singletonMap("pd", client));
        clients.init();

        try {
            clients.getSignatureVerifier("pd");
            fail("a client with no public key must not resolve to a verifier");
        } catch (AuthenticationException expected) {
            assertTrue("the refusal should name the client",
                    expected.getMessage() != null && expected.getMessage().contains("pd"));
        } catch (RuntimeException fault) {
            // Caught explicitly so the failure reads as the contract being broken rather than
            // as an incidental crash: an uncaught NullPointerException here is indistinguishable
            // from a broken test at a glance, and it is precisely what reaches the client as a
            // 500 today.
            fail("a client with no public key must be refused as an AuthenticationException,"
                    + " which HmacVerifyingFilter turns into a 401; it instead raised "
                    + fault.getClass().getName() + " (" + fault.getMessage() + "),"
                    + " which escapes the filter as a 500");
        }
    }

    @Test
    public void rejectsASignatureOverDifferentContent() throws Exception {

        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        ApiClients clients = clientsWith("RSA", "SHA256withRSA", pair);

        List<byte[]> signed = Collections.singletonList("GET/api/s2s/x".getBytes("UTF-8"));
        List<byte[]> tampered = Collections.singletonList("GET/api/s2s/y".getBytes("UTF-8"));

        assertFalse("a signature must not verify against different content",
                clients.getSignatureVerifier("pd").apply(tampered, sign(pair, "SHA256withRSA", signed)));
    }

    @Test
    public void rejectsAnAbsentSignature() throws Exception {

        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        ApiClients clients = clientsWith("RSA", "SHA256withRSA", pair);

        assertFalse(clients.getSignatureVerifier("pd")
                .apply(Collections.singletonList("x".getBytes("UTF-8")), null));
    }

    private void assertRoundTrip(String keyAlgorithm, String signatureAlgorithm) throws Exception {

        KeyPair pair = KeyPairGenerator.getInstance(keyAlgorithm).generateKeyPair();
        ApiClients clients = clientsWith(keyAlgorithm, signatureAlgorithm, pair);

        List<byte[]> factors = Collections.singletonList("GET/api/s2s/vocabularies".getBytes("UTF-8"));

        assertTrue(signatureAlgorithm + " should verify",
                clients.getSignatureVerifier("pd").apply(factors, sign(pair, signatureAlgorithm, factors)));
    }

    private ApiClients clientsWith(String keyAlgorithm, String signatureAlgorithm, KeyPair pair) {

        ApiClients.ApiClient client = new ApiClients.ApiClient();
        client.setAlgorithm(signatureAlgorithm);
        client.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        ApiClients clients = new ApiClients();
        ReflectionTestUtils.setField(clients, "clients",
                Collections.singletonMap("pd", client));
        assertEquals(keyAlgorithm, pair.getPublic().getAlgorithm());
        return clients;
    }

    private String sign(KeyPair pair, String algorithm, List<byte[]> factors) throws Exception {

        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(pair.getPrivate());
        for (byte[] factor : factors) {
            signature.update(factor);
        }
        return Base64.getEncoder().encodeToString(signature.sign());
    }
}
