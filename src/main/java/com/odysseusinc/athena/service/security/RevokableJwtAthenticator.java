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

package com.odysseusinc.athena.service.security;

import java.util.List;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

public class RevokableJwtAthenticator extends JwtAuthenticator {

    private RevokedTokenStore tokenStore;

    public RevokableJwtAthenticator(String signingSecret, RevokedTokenStore tokenStore) {

        super(new SecretSignatureConfiguration(signingSecret));
        this.tokenStore = tokenStore;
    }


    public RevokableJwtAthenticator(List<SignatureConfiguration> signatureConfigurations,
                                    List<EncryptionConfiguration> encryptionConfigurations, RevokedTokenStore tokenStore) {

        super(signatureConfigurations, encryptionConfigurations);
        this.tokenStore = tokenStore;
    }

    public RevokableJwtAthenticator(SignatureConfiguration signatureConfiguration,
                                    EncryptionConfiguration encryptionConfiguration, RevokedTokenStore tokenStore) {

        super(signatureConfiguration, encryptionConfiguration);
        this.tokenStore = tokenStore;
    }

    @Override
    public void validate(TokenCredentials credentials, WebContext context) throws HttpAction, CredentialsException {

        String token = credentials.getToken();
        if (tokenStore.contains(token)) {
            throw new CredentialsException("Token was revoked");
        }
        super.validate(credentials, context);
    }
}
