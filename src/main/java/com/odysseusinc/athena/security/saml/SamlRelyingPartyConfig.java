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
     * The endpoint the IdP already has registered for this SP. Taken verbatim from the
     * deployed {@code sp-metadata.xml}, which declares
     * {@code {base}/auth/callback?client_name=SAML2Client}. The query parameter is a
     * pac4j artefact; Spring Security matches the assertion consumer service on path
     * only, so keeping this path means <strong>no IdP reconfiguration and no metadata
     * regeneration</strong> is needed.
     */
    public static final String ASSERTION_CONSUMER_SERVICE_PATH = "/auth/callback";

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

        return RelyingPartyRegistrations
                .fromMetadataLocation(idpMetadataLocation)
                .registrationId(REGISTRATION_ID)
                .entityId(serviceProviderEntityId)
                .assertionConsumerServiceLocation("{baseUrl}" + ASSERTION_CONSUMER_SERVICE_PATH)
                .signingX509Credentials(credentials -> credentials.add(loadSigningCredential()))
                .build();
    }

    /**
     * Loads the SP signing keypair. The deployed SP metadata declares
     * {@code AuthnRequestsSigned="true"}, so this credential is required for the IdP to
     * accept an authentication request.
     */
    private Saml2X509Credential loadSigningCredential() {

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

            return Saml2X509Credential.signing(privateKey, certificate);

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
