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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.security.RevokedTokenStore;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is the start of it.
 * <p>
 * JUnit 4 on purpose — surefire 2.19.1 has no JUnit 5 provider, so a jupiter test
 * here would silently never run.
 */
public class JwtTokenServiceTest {

    /** Must be >= 32 bytes for HS256. */
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String OTHER_SECRET = "fedcba9876543210fedcba9876543210";

    private InMemoryRevokedTokenStore revoked;
    private JwtTokenService service;

    @Before
    public void setUp() {

        revoked = new InMemoryRevokedTokenStore();
        service = new JwtTokenService(SECRET, 3600, revoked);
    }

    @Test
    public void issuesATokenThatValidatesBackToTheSameUser() {

        String token = service.issue(user("jane", "saml"));

        Optional<JwtTokenService.TokenIdentity> identity = service.validate(token);

        assertTrue("a freshly issued token must validate", identity.isPresent());
        assertEquals("jane", identity.get().getUsername());
        assertEquals("saml", identity.get().getOrigin());
    }

    @Test
    public void rejectsATokenSignedWithADifferentSecret() {

        String foreign = new JwtTokenService(OTHER_SECRET, 3600, revoked).issue(user("jane", "saml"));

        assertFalse("a token signed with another secret must not be accepted",
                service.validate(foreign).isPresent());
    }

    @Test
    public void rejectsATamperedSubject() throws Exception {

        // re-sign a forged claim set with the wrong key, keeping a valid JWT shape
        JWTClaimsSet forged = new JWTClaimsSet.Builder()
                .subject("attacker")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), forged);
        jwt.sign(new MACSigner(OTHER_SECRET.getBytes("UTF-8")));

        assertFalse(service.validate(jwt.serialize()).isPresent());
    }

    @Test
    public void rejectsAnExpiredToken() {

        JwtTokenService shortLived = new JwtTokenService(SECRET, -1, revoked);

        assertFalse("a token past its expiry must not be accepted",
                shortLived.validate(shortLived.issue(user("jane", "saml"))).isPresent());
    }

    @Test
    public void rejectsARevokedToken() {

        String token = service.issue(user("jane", "saml"));
        assertTrue(service.validate(token).isPresent());

        service.revoke(token);

        assertFalse("a token revoked at logout must not be accepted",
                service.validate(token).isPresent());
    }

    /**
     * The shape behind CVE-2026-29000: a JWE wrapping an unsigned PlainJWT. The old
     * pac4j JwtAuthenticator could be induced to trust the inner claims without
     * verifying a signature. Nothing here decrypts, so it must simply be refused.
     */
    @Test
    public void rejectsAJweWrappedUnsignedToken() throws Exception {

        PlainJWT inner = new PlainJWT(new JWTClaimsSet.Builder()
                .subject("attacker")
                .claim("origin", "saml")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                .build());

        EncryptedJWT jwe = new EncryptedJWT(
                new JWEHeader(JWEAlgorithm.A256KW, EncryptionMethod.A128CBC_HS256),
                inner.getJWTClaimsSet());
        jwe.encrypt(new AESEncrypter(SECRET.getBytes("UTF-8")));

        assertFalse("a JWE-wrapped token must never authenticate anyone",
                service.validate(jwe.serialize()).isPresent());
    }

    /** An unsigned token must not be accepted even when the claims look right. */
    @Test
    public void rejectsAnUnsignedToken() {

        PlainJWT plain = new PlainJWT(new JWTClaimsSet.Builder()
                .subject("attacker")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                .build());

        assertFalse(service.validate(plain.serialize()).isPresent());
    }

    @Test
    public void rejectsGarbageWithoutThrowing() {

        assertFalse(service.validate(null).isPresent());
        assertFalse(service.validate("").isPresent());
        assertFalse(service.validate("not-a-jwt").isPresent());
        assertFalse(service.validate("a.b.c").isPresent());
    }

    /**
     * A short secret cannot produce a secure HS256 signature, so the application
     * should refuse to start rather than sign with it. 
     */
    @Test
    public void refusesToStartWithATooShortSecret() {

        for (String weak : new String[]{null, "", "short", "sssshhhh!"}) {
            try {
                new JwtTokenService(weak, 3600, revoked);
                fail("expected a secret of " + (weak == null ? "null" : "'" + weak + "'")
                        + " to be rejected");
            } catch (IllegalStateException expected) {
                // wanted
            }
        }
    }

    private AthenaUser user(String username, String origin) {

        AthenaUser user = new AthenaUser();
        user.setUsername(username);
        user.setOrigin(origin);
        return user;
    }

    private static final class InMemoryRevokedTokenStore implements RevokedTokenStore {

        private final Set<String> tokens = new HashSet<>();

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
