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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Configuration and cryptographic primitives for verification of HMAC signatures.
 * Multiple clients can be configured with separate keys.
 * At minimum, client needs tp have algorithm and public key (BASE64 X509 container) configured.
 * Adding private key to configuration (BASE64 PKCS8 container) will trigger logging correct signature for a mismatch.
 * This feature is intended for troubleshooting only and must not be used in production environments.
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
    /**
     * Key is client id. Value is base64 encoded key.
     */
    private Map<String, ApiClient> clients;
    /**
     * Time tolerance for checking nonce. Defaults to 1 minute. Override is intended mostly for manual testing.
     */
    private Duration timeTolerance = Duration.ofMinutes(1);

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        clients.forEach((clientId, client) -> {
            String publicKey = client.getPublicKey();
            if (publicKey == null) {
                generateTransientPair(client);
                log.error(
                        "Missing public key for client id [{}], generated a transient pair. This pair is good for debugging, " +
                        "is not stored anywhere and MUST NOT be used in production cofiguration. The pair is printed below, public key first\n{}\n{}",
                        clientId, client.getPublicKey(), client.getPrivateKey()
                );
            }
        });
        log.info("Initialized with {} clients and time tolerance {}", clients.size(), timeTolerance.toString());
    }

    public BiFunction<List<byte[]>, String, Boolean> getSignatureVerifier(String clientId) {
        ApiClient client = Optional.ofNullable(clients.get(clientId)).orElseThrow(() -> new AuthenticationCredentialsNotFoundException(clientId));
        String publicKey = client.getPublicKey();
        PublicKey key = parseKey(publicKey);

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
        Optional.ofNullable(client.getPrivateKey()).ifPresent(privateKey -> {
            log.info("Correct signature: " + calculateCorrectSignature(client, factors, privateKey));
        });
    }

    private String calculateCorrectSignature(ApiClient client, List<byte[]> factors, String key) {
        try {
            KeyFactory kf = KeyFactory.getInstance(DSA);
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

    private static PublicKey parseKey(String base64key) {
        byte[] key = BASE64DECODER.decode(base64key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        try {
            return KeyFactory.getInstance(DSA).generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key spec: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm DSA not found", e);
        }
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
