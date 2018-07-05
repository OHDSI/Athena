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

import static org.pac4j.core.util.CommonHelper.assertNotNull;

import com.odysseusinc.athena.model.security.AthenaProfile;
import com.odysseusinc.athena.service.security.RevokedTokenStore;
import com.odysseusinc.athena.util.UserProfileUtil;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.redirect.RedirectAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomLogoutLogic<R, C extends WebContext> extends DefaultLogoutLogic<R, C> {

    private final RevokedTokenStore tokenStore;

    @Autowired
    public CustomLogoutLogic(RevokedTokenStore tokenStore) {

        this.tokenStore = tokenStore;
    }

    @Override
    public R perform(C context, Config config,
                     HttpActionAdapter<R, C> httpActionAdapter,
                     String defaultUrl, String inputLogoutUrlPattern,
                     Boolean inputLocalLogout, Boolean inputDestroySession, Boolean inputCentralLogout) {

        // default values
        final String logoutUrlPattern;
        if (inputLogoutUrlPattern == null) {
            logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;
        } else {
            logoutUrlPattern = inputLogoutUrlPattern;
        }
        final boolean localLogout;
        if (inputLocalLogout == null) {
            localLogout = true;
        } else {
            localLogout = inputLocalLogout;
        }
        final boolean destroySession;
        if (inputDestroySession == null) {
            destroySession = false;
        } else {
            destroySession = inputDestroySession;
        }
        final boolean centralLogout;
        if (inputCentralLogout == null) {
            centralLogout = false;
        } else {
            centralLogout = inputCentralLogout;
        }

        Object sessionAttribute = context.getSessionAttribute(Pac4jConstants.USER_PROFILES);
        if (sessionAttribute instanceof Map) {
            Map<String, CommonProfile> profileMap = (Map<String, CommonProfile>) sessionAttribute;
            profileMap.forEach((k, v) -> {
                if (v instanceof AthenaProfile) {
                    tokenStore.invalidate(((AthenaProfile) v).getToken());
                }
            });
        } else if (sessionAttribute instanceof AthenaProfile) {
            AthenaProfile profile = (AthenaProfile) sessionAttribute;
            tokenStore.invalidate(profile.getToken());
        }

        final Clients configClients = config.getClients();
        assertNotNull("configClients", configClients);

        // logic
        final ProfileManager manager = getProfileManager(context, config);
        final List<CommonProfile> profiles = manager.getAll(true);

        final String url = context.getRequestParameter(Pac4jConstants.URL);
        String redirectUrl = defaultUrl;
        if (url != null && Pattern.matches(logoutUrlPattern, url)) {
            redirectUrl = url;
        }
        HttpAction action;
        if (redirectUrl != null) {
            action = HttpAction.redirect("redirect", context, redirectUrl);
        } else {
            action = HttpAction.ok("ok", context);
        }

        // local logout if requested or multiple profiles
        if (localLogout || profiles.size() > 1) {
            logger.debug("Performing application logout");
            manager.logout();
            if (destroySession) {
                final SessionStore sessionStore = context.getSessionStore();
                if (sessionStore != null) {
                    final boolean removed = sessionStore.destroySession(context);
                    if (!removed) {
                        logger.error("Unable to destroy the web session. The session store may not support this feature");
                    }
                } else {
                    logger.error("No session store available for this web context");
                }
            }
        }

        SecurityContextHolder.clearContext();

        if (centralLogout) {
            logger.debug("Performing central logout");
            for (final CommonProfile profile : profiles) {
                logger.debug("Profile: {}", profile);
                String clientName;
                if (profile instanceof AthenaProfile) {
                    clientName = "SAML2Client";
                } else {
                    clientName = profile.getClientName();
                }
                if (clientName != null) {
                    final Client client = configClients.findClient(clientName);
                    if (client != null) {
                        final String targetUrl;
                        if (redirectUrl != null && (redirectUrl.startsWith(HttpConstants.SCHEME_HTTP)
                                || redirectUrl.startsWith(HttpConstants.SCHEME_HTTPS))) {
                            targetUrl = redirectUrl;
                        } else {
                            targetUrl = null;
                        }
                        final RedirectAction logoutAction = client.getLogoutAction(context, profile, targetUrl);
                        logger.debug("Logout action: {}", logoutAction);
                        if (logoutAction != null) {
                            action = logoutAction.perform(context);
                        }
                    }
                }
            }
        }
        return httpActionAdapter.adapt(action.getCode(), context);
    }
}
