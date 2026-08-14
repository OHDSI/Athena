package com.odysseusinc.athena.security.hmac;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Configuration and cryptographic primitives for verification of HMAC signatures.
 * Multiple clients can be configured with separate keys.
 * At minimum, client needs tp have algorithm and public key (BASE64 X509 container) configured.
 * Setting {@code api.debug-signatures=true} additionally logs the correct signature whenever one
 * is rejected, using the private key (BASE64 PKCS8 container) configured for that client. Both
 * are required and the flag defaults to false, so a private key left in a configuration on its
 * own does nothing. Local troubleshooting only: such a log line is enough to forge the request.
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties("api")
public class ApiClients {
    private static final Base64.Decoder BASE64DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE64ENCODER = Base64.getEncoder();
    private static final String DSA = "DSA";
    /** Signature algorithms whose name is also the key algorithm — they contain no "with". */
    private static final String[] SELF_NAMED_KEY_ALGORITHMS = {"Ed25519", "Ed448", "EdDSA", "RSASSA-PSS"};
    /** Encoding suffix on names such as {@code SHA256withECDSAinP1363Format}. */
    private static final String P1363 = "inP1363Format";
    /**
     * Key is client id. Value is base64 encoded key.
     */
    private Map<String, ApiClient> clients = new LinkedHashMap<>();
    /**
     * Time tolerance for checking nonce. Defaults to 1 minute. Override is intended mostly for manual testing.
     */
    private Duration timeTolerance = Duration.ofMinutes(1);

    /**
     * Enables the local troubleshooting aids, which write key material to the application log:
     * a correct signature whenever one is rejected, and a generated keypair for a client whose
     * public key is missing. A log line containing a valid signature is enough to replay or
     * forge that request, so this is off unless asked for explicitly.
     * <p>
     * Configuring a private key is no longer sufficient on its own. That was the whole
     * mechanism before, which meant a private key left in a deployed configuration kept
     * leaking silently; now the key is ignored and the fact is logged at startup.
     */
    private boolean debugSignatures = false;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // this map is only populated when api.clients.* is configured. It used to
        // default to null, so a deployment without that configuration failed here with a
        // bare NullPointerException during ApplicationReadyEvent — i.e. startup aborted
        // with no indication of the cause. Empty now, with the reason stated.
        if (clients.isEmpty()) {
            log.warn("No api.clients.* configured: server-to-server requests to /api/s2s/** "
                    + "cannot be authenticated and will be rejected.");
        }
        if (debugSignatures) {
            log.warn("api.debug-signatures is enabled. Correct signatures and generated "
                    + "keypairs will be written to the application log, which is enough to "
                    + "forge server-to-server requests. Local troubleshooting only.");
        }
        clients.forEach((clientId, client) -> {
            if (client.getPublicKey() == null) {
                if (debugSignatures) {
                    generateTransientPair(client);
                    log.error(
                            "Missing public key for client id [{}], generated a transient pair. This pair is good for debugging, " +
                            "is not stored anywhere and MUST NOT be used in production cofiguration. The pair is printed below, public key first\n{}\n{}",
                            clientId, client.getPublicKey(), client.getPrivateKey()
                    );
                } else {
                    // Fabricating a keypair here never made the client work — it cannot sign
                    // with a key it was never given — it only printed one. Say what is wrong
                    // instead; the request itself fails when the client is actually used.
                    log.error("No public key configured for client id [{}]: its requests to "
                            + "/api/s2s/** cannot be authenticated. Set api.clients.{}.public-key.",
                            clientId, clientId);
                }
            }
        });
        clients.forEach((clientId, client) -> {
            if (client.getPrivateKey() != null && !debugSignatures) {
                log.warn("Client id [{}] has a private key configured. It is being ignored: "
                        + "signature diagnostics require api.debug-signatures=true. A private "
                        + "key does not belong in a deployed configuration — remove it.", clientId);
            }
        });
        log.info("Initialized with {} clients and time tolerance {}", clients.size(), timeTolerance.toString());
    }

    public BiFunction<List<byte[]>, String, Boolean> getSignatureVerifier(String clientId) {
        ApiClient client = Optional.ofNullable(clients.get(clientId)).orElseThrow(() -> new AuthenticationCredentialsNotFoundException(clientId));
        String publicKey = client.getPublicKey();
        PublicKey key = parseKey(publicKey, client.getAlgorithm());

        try {
            Signature sgn = Signature.getInstance(client.getAlgorithm());
            sgn.initVerify(key);
            return (factors, signature) -> {
                try {
                    if (signature == null) {
                        throw new SignatureException("Empty signature");
                    }
                    for (byte[] factor : factors) {
                        sgn.update(factor);
                    }
                    boolean result = sgn.verify(BASE64DECODER.decode(signature));
                    if (!result) {
                        log.info("Incorrect signature [" + signature + "]");
                        logValidSignatureIfRequired(client, factors);
                    }
                    return result;
                } catch (SignatureException e) {
                    log.info("Invalid signature [" + signature + "], error ", e);
                    logValidSignatureIfRequired(client, factors);
                    return false;
                }
            };
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Configuration error: " + e.getMessage(), e);
        }
    }

    private void logValidSignatureIfRequired(ApiClient client, List<byte[]> factors) {
        if (!debugSignatures) {
            return;
        }
        Optional.ofNullable(client.getPrivateKey()).ifPresent(privateKey -> {
            log.info("Correct signature: " + calculateCorrectSignature(client, factors, privateKey));
        });
    }

    private String calculateCorrectSignature(ApiClient client, List<byte[]> factors, String key) {
        try {
            // Derived from the client's signature algorithm for the same reason the public-key
            // factory is: hardcoding DSA here fails for any client configured otherwise.
            KeyFactory kf = KeyFactory.getInstance(keyAlgorithmOf(client.getAlgorithm()));
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            PrivateKey privateKey = kf.generatePrivate(keySpecPKCS8);
            Signature sgn = Signature.getInstance(client.getAlgorithm());
            sgn.initSign(privateKey);
            for (byte[] factor : factors) {
                sgn.update(factor);
            }
            byte[] bytes = sgn.sign();
            return BASE64ENCODER.encodeToString(bytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidKeySpecException e) {
            throw new RuntimeException("Configuration error: " + e.getMessage(), e);
        }
    }

    /**
     * the key factory used to be hardcoded to DSA even though the signature
     * algorithm is per-client configuration, so any client configured with, say,
     * {@code SHA256withRSA} could not have its public key parsed at all. The key algorithm
     * is now derived from the signature algorithm, falling back to DSA so existing
     * DSA-based clients are unaffected.
     */
    private static PublicKey parseKey(String base64key, String signatureAlgorithm) {
        String keyAlgorithm = keyAlgorithmOf(signatureAlgorithm);
        byte[] key = BASE64DECODER.decode(base64key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        try {
            return KeyFactory.getInstance(keyAlgorithm).generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key spec: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + keyAlgorithm + " not found", e);
        }
    }

    /**
     * Maps a JCA <em>signature</em> algorithm to the <em>key</em> algorithm its
     * {@link KeyFactory} is registered under. The two names coincide often enough to be
     * misleading but not always:
     * <ul>
     *   <li>{@code SHA256withRSA} -> {@code RSA} and {@code SHA1withDSA} -> {@code DSA}, where
     *       the suffix happens to be the key algorithm;</li>
     *   <li>{@code SHA256withECDSA} -> <b>{@code EC}</b>, not {@code ECDSA} — no standard
     *       provider registers a {@code KeyFactory} called {@code ECDSA};</li>
     *   <li>{@code Ed25519}, {@code Ed448} and {@code RSASSA-PSS} contain no {@code with} at
     *       all and are their own key algorithm.</li>
     * </ul>
     * Anything unrecognised that does contain {@code with} keeps the suffix, which is the
     * best available guess; anything else is rejected rather than silently treated as DSA,
     * because a wrong key algorithm fails later as an opaque {@code InvalidKeySpecException}.
     */
    static String keyAlgorithmOf(String signatureAlgorithm) {
        if (signatureAlgorithm == null || signatureAlgorithm.isBlank()) {
            return DSA;
        }
        String algorithm = signatureAlgorithm.trim();
        for (String selfNamed : SELF_NAMED_KEY_ALGORITHMS) {
            if (selfNamed.equalsIgnoreCase(algorithm)) {
                return selfNamed;
            }
        }
        int with = algorithm.toLowerCase().lastIndexOf("with");
        if (with < 0) {
            throw new IllegalArgumentException("Cannot derive a key algorithm from signature"
                    + " algorithm [" + signatureAlgorithm + "]. Configure api.clients.*.algorithm"
                    + " with a standard JCA signature algorithm name.");
        }
        String suffix = algorithm.substring(with + "with".length());
        // SHA1withDSAinP1363Format and SHA256withECDSAinP1363Format name an encoding, not a
        // different key type.
        if (suffix.length() > P1363.length()
                && suffix.regionMatches(true, suffix.length() - P1363.length(),
                                        P1363, 0, P1363.length())) {
            suffix = suffix.substring(0, suffix.length() - P1363.length());
        }
        return "ECDSA".equalsIgnoreCase(suffix) ? "EC" : suffix;
    }

    private static void generateTransientPair(ApiClient client) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(DSA);
            keyPairGen.initialize(2048);
            KeyPair pair = keyPairGen.generateKeyPair();
            String privKey = BASE64ENCODER.encodeToString(pair.getPrivate().getEncoded());
            client.setPrivateKey(privKey);
            String pubKey = BASE64ENCODER.encodeToString(pair.getPublic().getEncoded());
            client.setPublicKey(pubKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Configuration error: " + e.getMessage(), e);
        }
    }


    @Getter
    @Setter
    public static class ApiClient {
        private String algorithm;
        private String publicKey;
        /**
         * For debugging purposes only, adding this triggers calculation of the correct signature and logging it to application log
         */
        private String privateKey;
    }

}
