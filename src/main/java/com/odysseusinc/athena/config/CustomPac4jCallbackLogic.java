/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class CustomPac4jCallbackLogic<R, C extends WebContext> implements CallbackLogic<R, C> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${salt}")
    private String salt;

    @Value("${athena.async-auth-redirect}")
    private String redirectPath;

    private JwtGenerator<CommonProfile> jwtGenerator;

    public CustomPac4jCallbackLogic(JwtGenerator<CommonProfile> jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    public R perform(
            C context,
            Config config,
            HttpActionAdapter<R, C> httpActionAdapter,
            String inputDefaultUrl,
            Boolean inputMultiProfile,
            Boolean inputRenewSession) {

        this.logger.debug("=== CALLBACK ===");

        Clients clients = config.getClients();
        Client client = clients.findClient(context);

        HttpAction action;
        try {
            Credentials credentials = client.getCredentials(context);
            CommonProfile profile = client.getUserProfile(credentials, context);

            String token = jwtGenerator.generate(profile);

            logger.debug("jwt: " + token);

            action = HttpAction.redirect(
                    "redirect",
                    context,
                    redirectPath + "?token=" + token
            );
        } catch (HttpAction var15) {
            this.logger.debug("extra HTTP action required in callback: {}", Integer.valueOf(var15.getCode()));
            action = var15;
        }

        return httpActionAdapter.adapt(action.getCode(), context);
    }
}
