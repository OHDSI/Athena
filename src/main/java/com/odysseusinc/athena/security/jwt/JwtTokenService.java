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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.security.RevokedTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and validates the JWTs Athena uses as its session token.
 * <p>
 * Replaces pac4j's {@code JwtGenerator} plus {@code RevokableJwtAthenticator}.
 * Signing is HMAC-SHA256 over the {@code salt} secret, as before.
 * <p>
 * Deliberately does <em>not</em> support encrypted (JWE) tokens. The previous
 * setup declared a {@code SecretEncryptionConfiguration} bean but never wired it
 * into the authenticator, so encrypted tokens were never actually accepted.
 * Refusing anything that is not a plain signed JWT also sidesteps the class of
 * bug behind CVE-2026-29000, where a JWE wrapping an unsigned token could bypass
 * signature verification.
 */
@Slf4j
@Service
public class JwtTokenService {

    /** Minimum length for an HMAC-SHA256 secret, per RFC 7518 §3.2. */
    private static final int MIN_SECRET_BYTES = 32;

    private final byte[] secret;
    private final Duration expiration;
    private final RevokedTokenStore revokedTokenStore;

    public JwtTokenService(@Value("${salt}") String salt,
                           @Value("${athena.token.expiration:604800}") long expirationSeconds,
                           RevokedTokenStore revokedTokenStore) {

        if (salt == null || salt.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "The 'salt' property must be at least " + MIN_SECRET_BYTES
                            + " bytes to sign tokens with HMAC-SHA256; it is "
                            + (salt == null ? "not set" : "too short"));
        }
        this.secret = salt.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.expiration = Duration.ofSeconds(expirationSeconds);
        this.revokedTokenStore = revokedTokenStore;
    }

    /**
     * Mints a token for a user who has just authenticated.
     */
    public String issue(AthenaUser user) {

        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .claim(Claims.ORIGIN, user.getOrigin())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(expiration)))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(new MACSigner(secret));
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to sign the authentication token", e);
        }
        return jwt.serialize();
    }

    /**
     * Validates a presented token: signature, expiry, and revocation.
     *
     * @return the subject and origin identifying the user, or empty if the token
     *         is unusable for any reason. Never throws for untrusted input.
     */
    public Optional<TokenIdentity> validate(String token) {

        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        if (revokedTokenStore.contains(token)) {
            log.debug("Rejected a token that was revoked at logout");
            return Optional.empty();
        }
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                log.debug("Rejected a token signed with unexpected algorithm {}",
                        jwt.getHeader().getAlgorithm());
                return Optional.empty();
            }
            JWSVerifier verifier = new MACVerifier(secret);
            if (!jwt.verify(verifier)) {
                log.debug("Rejected a token whose signature did not verify");
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiresAt = claims.getExpirationTime();
            if (expiresAt == null || expiresAt.toInstant().isBefore(Instant.now())) {
                log.debug("Rejected an expired token");
                return Optional.empty();
            }
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) {
                log.debug("Rejected a token with no subject");
                return Optional.empty();
            }
            return Optional.of(new TokenIdentity(subject, claims.getStringClaim(Claims.ORIGIN)));

        } catch (ParseException e) {
            // Covers a JWE or any other non-signed-JWT shape as well as plain garbage.
            log.debug("Rejected an unparseable token: {}", e.getMessage());
            return Optional.empty();
        } catch (JOSEException e) {
            log.debug("Rejected a token that could not be verified: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Marks a token as no longer usable. Idempotent.
     */
    public void revoke(String token) {

        revokedTokenStore.invalidate(token);
    }

    /** Claim names. {@code origin} distinguishes users federated from different IdPs. */
    static final class Claims {
        static final String ORIGIN = "origin";

        private Claims() {
        }
    }

    /**
     * The identity a valid token asserts. {@code username} + {@code origin} is the
     * natural key of {@link AthenaUser}.
     */
    public static final class TokenIdentity {

        private final String username;
        private final String origin;

        public TokenIdentity(String username, String origin) {

            this.username = username;
            this.origin = origin;
        }

        public String getUsername() {

            return username;
        }

        public String getOrigin() {

            return origin;
        }
    }
}
