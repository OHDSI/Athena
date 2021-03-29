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

import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class CustomPac4jCallbackLogic<R, C extends WebContext> implements CallbackLogic<R, C> {

    @Value("${salt}")
    private String salt;

    @Value("${athena.async-auth-redirect}")
    private String redirectPath;

    private final JwtGenerator<CommonProfile> jwtGenerator;

    public CustomPac4jCallbackLogic(JwtGenerator<CommonProfile> jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public R perform(
            C context,
            Config config,
            HttpActionAdapter<R, C> httpActionAdapter,
            String inputDefaultUrl,
            Boolean inputMultiProfile,
            Boolean inputRenewSession,
            Boolean renewSession,
            String clientName
    ) {

        log.debug("=== CALLBACK ===");

        Clients clients = config.getClients();
        Client client = clients.findClient(SAML2Client.class);

        HttpAction action;
        try {
            Credentials credentials = client.getCredentials(context);
            CommonProfile profile = client.getUserProfile(credentials, context);

            String token = jwtGenerator.generate(profile);

            log.debug("jwt: " + token);

            action = HttpAction.redirect(
                    context,
                    redirectPath + "?token=" + token
            );
        } catch (HttpAction ex) {
            log.debug("extra HTTP action required in callback: {}", Integer.valueOf(ex.getCode()));
            action = ex;
        }

        return httpActionAdapter.adapt(action.getCode(), context);
    }
}
