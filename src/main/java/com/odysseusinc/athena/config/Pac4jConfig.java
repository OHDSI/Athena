/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.config;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.security.RevokableJwtAthenticator;
import com.odysseusinc.athena.service.security.RevokedTokenStore;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class Pac4jConfig {

    @Value("${salt}")
    private String salt;

    @Value("${cas.key-manager.store-password}")
    private String keyStorePassword;

    @Value("${cas.key-manager.passwords.arachnenetwork}")
    private String privateKeyPassword;

    @Value("${cas.entityId}")
    private String identityProviderEntityId;

    @Value("${cas.idpMetadataLocation}")
    private String metadataLocation;

    @Value("${cas.key-manager.key-store-file}")
    private String keyStoreFile;

    @Value("${cas.key-manager.default-key}")
    private String alias;

    @Value("${athena.token.header}")
    private String authTokenHeader;

    @Value("${salt}")
    private String secret;

    @Value("${athena.security.saml.metadata-location}")
    private String spMetadataLocation;
    @Value("${athena.security.saml.callback-url}")
    private String callback;

    @Autowired
    private ResourceLoader resourceLoader;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public Config config(UserService userService, RevokedTokenStore tokenStore) {
        Resource keystorePath = resourceLoader.getResource(keyStoreFile);
        Resource metadataLocationPath = resourceLoader.getResource(metadataLocation);
        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration(
                keystorePath,
                alias,
                null,
                keyStorePassword,
                privateKeyPassword,
                metadataLocationPath);
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId(identityProviderEntityId);

        cfg.setServiceProviderMetadataPath(spMetadataLocation);
        cfg.setDestinationBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

        final SAML2Client saml2Client = new SAML2Client(cfg);

        HeaderClient headerClient = new HeaderClient(authTokenHeader, new RevokableJwtAthenticator(salt, tokenStore));

        headerClient.setProfileCreator(userService);
        headerClient.setAuthorizationGenerator(userService);

        final Clients clients = new Clients(callback,
                headerClient,
                saml2Client,
                new AnonymousClient()
        );
        final Config config = new Config(clients);

        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));

        return config;
    }

    @Bean
    public JwtGenerator jwtGenerator() {

        //        return new JwtGenerator((String) null); //Unencrypted, not secured
        //        return new JwtGenerator(secret); //failed with com.nimbusds.jose.JOSEException:
        // Couldn't create AES/GCM/NoPadding cipher: Illegal key size
        return new JwtGenerator(secretSignatureConfiguration(), null);
    }

    @Bean
    public SecretSignatureConfiguration secretSignatureConfiguration() {

        return new SecretSignatureConfiguration(secret);
    }

    @Bean
    public SecretEncryptionConfiguration secretEncryptionConfiguration() {

        SecretEncryptionConfiguration configuration = new SecretEncryptionConfiguration(secret);
        configuration.setMethod(EncryptionMethod.A128CBC_HS256);
        configuration.setAlgorithm(JWEAlgorithm.A256KW);
        return configuration;
    }
}
