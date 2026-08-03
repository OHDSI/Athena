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

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.security.jwt.JwtTokenService;
import com.odysseusinc.athena.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Completes a SAML login: provisions the local user from the assertion, mints a JWT,
 * and hands it to the front end.
 * <p>
 * Replaces {@code CustomPac4jCallbackLogic}. The browser is redirected to
 * {@code athena.async-auth-redirect} carrying the token, which is what AthenaUI reads.
 * <p>
 * The token is delivered in the URL <em>fragment</em> rather than the query string. Browsers
 * do not send a fragment to the server, so the token stays out of this application's access
 * log, out of any proxy in front of it, and out of the {@code Referer} header of requests the
 * landing page subsequently makes. A query parameter is visible in all three.
 * <p>
 * AthenaUI is compiled into this application's jar from the {@code ui} submodule, so the two
 * sides always ship together and there is no window in which one understands the fragment and
 * the other does not. The query parameter is therefore removed outright rather than accepted
 * alongside it.
 */
@Slf4j
@Component
public class SamlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Recorded as {@link AthenaUser#getOrigin()}. pac4j derived this from the
     * {@code authenticationMethod} assertion attribute and refused to provision a user
     * when it was missing. Existing rows were written with whatever that attribute
     * contained, so this must keep matching or every SSO user is provisioned afresh
     * instead of being recognised.
     */
    private static final String ATTR_AUTHENTICATION_METHOD = "authenticationMethod";

    /** Fragment key the front end reads the token from. Must match AthenaUI's LoginComplete. */
    static final String TOKEN_FRAGMENT_KEY = "token";

    private final UserService userService;
    private final JwtTokenService tokenService;
    private final String redirectUrl;

    public SamlAuthenticationSuccessHandler(UserService userService,
                                            JwtTokenService tokenService,
                                            @Value("${athena.async-auth-redirect}") String redirectUrl) {

        this.userService = userService;
        this.tokenService = tokenService;
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (!(authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal)) {
            throw new IllegalStateException("Expected a SAML principal but got "
                    + (authentication.getPrincipal() == null
                    ? "null" : authentication.getPrincipal().getClass().getName()));
        }
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();

        Map<String, String> attributes = flatten(principal);
        String username = principal.getName();
        String origin = attributes.get(ATTR_AUTHENTICATION_METHOD);

        AthenaUser user = userService.provisionUser(username, origin, attributes);
        String token = tokenService.issue(user);

        log.info("SAML login completed for user id [{}], origin [{}]", user.getId(), origin);

        response.sendRedirect(redirectUrlFor(token));
    }

    /**
     * Package-private so the redirect shape can be asserted without driving a whole SAML
     * exchange. A JWT is base64url plus dots, all of which are legal in a fragment, so the
     * value needs no escaping.
     */
    String redirectUrlFor(String token) {

        return UriComponentsBuilder.fromUriString(redirectUrl)
                .fragment(TOKEN_FRAGMENT_KEY + "=" + token)
                .build()
                .toUriString();
    }

    /**
     * SAML attributes are multi-valued; every consumer here wants a single string.
     * Takes the first value, which is what pac4j's {@code UserProfileUtil.getAttribute}
     * did for its {@code JSONArray} case.
     */
    private Map<String, String> flatten(Saml2AuthenticatedPrincipal principal) {

        Map<String, String> flattened = new HashMap<>();
        principal.getAttributes().forEach((name, values) -> {
            String first = firstNonNull(values);
            if (first != null) {
                flattened.put(name, first);
            }
        });
        return flattened;
    }

    private String firstNonNull(List<Object> values) {

        if (values == null) {
            return null;
        }
        return values.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::valueOf)
                .findFirst()
                .orElse(null);
    }
}
