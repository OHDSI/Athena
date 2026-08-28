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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

/**
 * SAML relying party (service provider) configuration, replacing pac4j's
 * {@code Pac4jConfig}.
 * <p>
 * <strong>The registration is resolved lazily, on first SAML use — deliberately.</strong>
 * pac4j's {@code SAML2Client} initialised its keystore on first use rather than at
 * construction, so today the application starts perfectly well with no SAML
 * configuration at all: {@code properties/{test,qa,prod}} all ship a blank keystore
 * alias and blank passwords (they are injected at deploy time), and the Cucumber
 * suite runs on the test profile. Building the registration eagerly in this
 * {@code @Bean} would therefore break application startup for the entire test suite
 * and for any developer who never touches SSO. Lazy resolution keeps that behaviour:
 * misconfiguration surfaces loudly when someone actually authenticates, not on boot.
 * <p>
 * Property names here are inherited and two of them are actively misleading:
 * <ul>
 *   <li>{@code cas.entityId} is the <em>service provider</em> entity id, not the IdP's
 *       (pac4j's own field for it was misnamed {@code identityProviderEntityId}).</li>
 *   <li>{@code cas.key-manager.passwords.arachnenetwork} is the <em>private key</em>
 *       password for whichever alias {@code cas.key-manager.default-key} names — in
 *       practice {@code apollo}. It has nothing to do with the {@code arachnenetwork}
 *       alias that also exists in the keystore.</li>
 * </ul>
 * Both are kept as-is so deployment configuration does not have to change.
 */
@Slf4j
@Configuration
public class SamlRelyingPartyConfig {

    /**
     * Only one relying party is ever registered. The value is not exposed in any URL
     * (see the assertion consumer service path below), so it is purely internal.
     */
    public static final String REGISTRATION_ID = "athena";

    /**
     * Assertion consumer service path. The deployed {@code sp-metadata.xml} must declare a
     * {@code Location} that resolves to exactly this URL, <strong>including any query
     * string</strong>: OpenSAML validates the assertion's {@code Destination} and
     * {@code Recipient} against the full URL, not the path alone. A mismatch bounces the
     * browser back to {@link #AUTHENTICATION_REQUEST_PATH}{@code ?error}, which presents as a
     * login loop rather than as an error.
     * <p>
     * Metadata generated for the previous pac4j-based implementation carries a
     * {@code ?client_name=SAML2Client} suffix on this location. That suffix is not reproduced
     * here, so such metadata has to be regenerated and re-registered with the identity
     * provider before this implementation will accept an assertion.
     */
    public static final String ASSERTION_CONSUMER_SERVICE_PATH = "/auth/callback";

    /**
     * Spring Security's authentication-request endpoint for this registration. Requesting it
     * builds a signed {@code AuthnRequest} and redirects the browser to the identity provider.
     * <p>
     * Wired as {@code saml2Login.loginPage} because {@link LazyRelyingPartyRegistrationRepository}
     * is not {@link Iterable}: both the single-provider auto-redirect and the default
     * {@code /login} chooser iterate the repository while the filter chain is being built,
     * which would force the credential and metadata load this class defers on purpose.
     */
    public static final String AUTHENTICATION_REQUEST_PATH =
            "/saml2/authenticate/" + REGISTRATION_ID;

    private final ResourceLoader resourceLoader;

    @Value("${cas.idpMetadataLocation}")
    private String idpMetadataLocation;

    /** Service provider entity id, despite the property name. */
    @Value("${cas.entityId}")
    private String serviceProviderEntityId;

    @Value("${cas.key-manager.key-store-file}")
    private String keyStoreFile;

    @Value("${cas.key-manager.store-password}")
    private String keyStorePassword;

    /** Keystore alias holding the SP signing key. */
    @Value("${cas.key-manager.default-key}")
    private String signingKeyAlias;

    /** Private key password for {@link #signingKeyAlias} — see the class javadoc. */
    @Value("${cas.key-manager.passwords.arachnenetwork}")
    private String signingKeyPassword;

    public SamlRelyingPartyConfig(ResourceLoader resourceLoader) {

        this.resourceLoader = resourceLoader;
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {

        return new LazyRelyingPartyRegistrationRepository(this::buildRegistration);
    }

    private RelyingPartyRegistration buildRegistration() {

        log.info("Initialising SAML relying party [{}] from IdP metadata [{}]",
                REGISTRATION_ID, idpMetadataLocation);

        KeyMaterial keyMaterial = loadKeyMaterial();

        return RelyingPartyRegistrations
                .fromMetadataLocation(idpMetadataLocation)
                .registrationId(REGISTRATION_ID)
                .entityId(serviceProviderEntityId)
                .assertionConsumerServiceLocation("{baseUrl}" + ASSERTION_CONSUMER_SERVICE_PATH)
                // The SP metadata declares AuthnRequestsSigned="true". Spring Security
                // otherwise signs only when the identity provider's metadata asks for it,
                // which leaves the two sides disagreeing whenever it does not.
                .authnRequestsSigned(true)
                .signingX509Credentials(credentials ->
                        credentials.add(Saml2X509Credential.signing(
                                keyMaterial.privateKey(), keyMaterial.certificate())))
                // The same keypair also decrypts, which is what pac4j's SAML2Client did with
                // this keystore. Registering only the signing half works right up until the
                // identity provider encrypts an assertion or NameID, and then fails at login
                // rather than at startup, because the registration is built lazily.
                .decryptionX509Credentials(credentials ->
                        credentials.add(Saml2X509Credential.decryption(
                                keyMaterial.privateKey(), keyMaterial.certificate())))
                .build();
    }

    /** The SP private key and its certificate, read once and used for both credentials. */
    private record KeyMaterial(PrivateKey privateKey, X509Certificate certificate) {
    }

    /**
     * Loads the SP keypair. The deployed SP metadata declares
     * {@code AuthnRequestsSigned="true"}, so this is required for the IdP to accept an
     * authentication request, and it is the key an IdP encrypts to.
     */
    private KeyMaterial loadKeyMaterial() {

        Resource keystore = resourceLoader.getResource(keyStoreFile);
        if (!keystore.exists()) {
            throw new IllegalStateException("No SAML signing keystore at [" + keyStoreFile
                    + "]. A keystore is not shipped with the application — point"
                    + " cas.key-manager.key-store-file at the one supplied at deployment time,"
                    + " and set cas.key-manager.store-password, cas.key-manager.default-key and"
                    + " cas.key-manager.passwords.arachnenetwork to match it.");
        }
        try (InputStream in = keystore.getInputStream()) {

            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(in, charsOf(keyStorePassword));

            PrivateKey privateKey = (PrivateKey) store.getKey(signingKeyAlias, charsOf(signingKeyPassword));
            if (privateKey == null) {
                throw new IllegalStateException("No private key for alias [" + signingKeyAlias
                        + "] in keystore [" + keyStoreFile
                        + "]. Check cas.key-manager.default-key and"
                        + " cas.key-manager.passwords.arachnenetwork (the key password).");
            }
            X509Certificate certificate = (X509Certificate) store.getCertificate(signingKeyAlias);
            if (certificate == null) {
                throw new IllegalStateException("No certificate for alias [" + signingKeyAlias
                        + "] in keystore [" + keyStoreFile + "]");
            }
            warnIfExpired(certificate);

            return new KeyMaterial(privateKey, certificate);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load the SAML signing key from [" + keyStoreFile + "]", e);
        }
    }

    /**
     * A deployment supplies its own keystore, so the certificate in it may have expired
     * without anyone noticing. The IdP will reject signed authentication requests if it has;
     * say so rather than failing obscurely later.
     */
    private void warnIfExpired(X509Certificate certificate) {

        try {
            certificate.checkValidity();
        } catch (Exception e) {
            log.error("SAML signing certificate for alias [{}] is not currently valid: {}."
                            + " The IdP will reject signed authentication requests."
                            + " Subject [{}], expires [{}].",
                    signingKeyAlias, e.getMessage(),
                    certificate.getSubjectDN(), certificate.getNotAfter());
        }
    }

    private static char[] charsOf(String password) {

        return password == null ? new char[0] : password.toCharArray();
    }

    /**
     * Defers building the registration until the first SAML request. See the class
     * javadoc for why this matters.
     * <p>
     * Deliberately <em>not</em> {@link Iterable}: Spring Security walks the repository while
     * building the filter chain, and that walk would force {@link #buildRegistration()} at
     * startup. The login entry point is pinned via {@link #AUTHENTICATION_REQUEST_PATH}
     * instead.
     */
    static final class LazyRelyingPartyRegistrationRepository
            implements RelyingPartyRegistrationRepository {

        private final Supplier<RelyingPartyRegistration> supplier;
        private volatile RelyingPartyRegistration registration;

        LazyRelyingPartyRegistrationRepository(Supplier<RelyingPartyRegistration> supplier) {

            this.supplier = supplier;
        }

        @Override
        public RelyingPartyRegistration findByRegistrationId(String registrationId) {

            if (!REGISTRATION_ID.equals(registrationId)) {
                return null;
            }
            RelyingPartyRegistration resolved = this.registration;
            if (resolved == null) {
                synchronized (this) {
                    resolved = this.registration;
                    if (resolved == null) {
                        resolved = supplier.get();
                        this.registration = resolved;
                    }
                }
            }
            return resolved;
        }
    }
}
