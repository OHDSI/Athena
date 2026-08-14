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

package com.odysseusinc.athena.controllers;

import com.odysseusinc.athena.security.saml.SamlAuthenticationSuccessHandler;
import com.odysseusinc.athena.security.saml.SamlRelyingPartyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class SSOController implements ApplicationContextAware {

    /** Landing page the identity provider returns the logout popup to. Must stay public. */
    public static final String LOGGED_OUT_PATH = "/auth/logged-out";

    private static final String LOGGED_OUT_PAGE =
            "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"/>"
                    + "<title>Signed out</title></head><body>"
                    + "<p>You have been signed out. You can close this window.</p>"
                    + "<script>window.close();</script>"
                    + "</body></html>";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SamlAuthenticationSuccessHandler samlAuthenticationSuccessHandler;

    @Value("${athena.security.saml.metadata-location}")
    private String metadataLocation;
    @Value("${athena.slo-url}")
    private String sloUri;

    /**
     * Optional external base URL, for deployments where the public address cannot be
     * derived from the request — a proxy that does not forward the original host, for
     * example. Left empty, the base URL is taken from the incoming request instead, so no
     * environment-specific address has to be configured at all.
     */
    @Value("${athena.url:}")
    private String configuredBaseUrl;

    /**
     * Login entry point, opened by the front end in a popup.
     * <p>
     * Unauthenticated browsers never reach this method: {@code WebSecurityConfig} requires
     * authentication on the path, so Spring Security diverts them to
     * {@link SamlRelyingPartyConfig#AUTHENTICATION_REQUEST_PATH} first.
     * <p>
     * They do reach it on a <em>second</em> login, because the session established by the
     * first one is still authenticated. There is no view behind this path, so that used to
     * end in {@code NoResourceFoundException}. Re-running the success handler issues a fresh
     * token instead, which is what the front end is waiting for.
     */
    @GetMapping("/auth/sso")
    public void startSso(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal) {
            samlAuthenticationSuccessHandler.onAuthenticationSuccess(
                    request, response, authentication);
            return;
        }
        response.sendRedirect(SamlRelyingPartyConfig.AUTHENTICATION_REQUEST_PATH);
    }

    /**
     * Serves this service provider's metadata, when a copy of it is packaged.
     * <p>
     * It is deployment data and normally is not: the registered copy lives with the identity
     * provider. Unset, or pointing at something absent, this answers 404 rather than failing —
     * the endpoint is a convenience, and nothing in the login flow goes through it.
     */
    @GetMapping("/auth/saml-metadata")
    @ResponseBody
    public void samlMetadata(HttpServletResponse response) throws IOException {

        if (metadataLocation == null || metadataLocation.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ClassPathResource resource = new ClassPathResource(metadataLocation);
        if (!resource.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final InputStream is = resource.getInputStream();
        response.setContentType("application/xml");
        response.setHeader("Content-type", "application/xml");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Logout entry point, opened by the front end in a popup.
     * <p>
     * The local session is dropped first. Without that the session stays authenticated after
     * the identity provider has signed the user out, so the next login would re-issue a token
     * for the previous user without any credentials being presented — the failure mode that
     * shows up as being unable to switch accounts.
     * <p>
     * The browser is then sent to the identity provider's logout URL carrying a
     * {@code service} parameter, so a provider configured to follow service redirects returns
     * the popup to {@link #LOGGED_OUT_PATH}, which closes it.
     */
    @GetMapping("/auth/slo")
    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.sendRedirect(logoutRedirectUrl(loggedOutUrl(request)));
    }

    /** Closes the logout popup. The main window clears its own token locally. */
    @GetMapping(LOGGED_OUT_PATH)
    public void loggedOut(HttpServletResponse response) throws IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write(LOGGED_OUT_PAGE);
    }

    /**
     * Absolute URL of {@link #LOGGED_OUT_PATH}. Derived from the request unless a base URL
     * is configured, so nothing environment-specific has to be hard-coded.
     * <p>
     * When TLS terminates at a proxy this depends on {@code server.forward-headers-strategy}
     * being set: otherwise the request reports the internal scheme and the identity provider
     * is handed an {@code http} address it will refuse to match.
     */
    private String loggedOutUrl(HttpServletRequest request) {

        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return stripTrailingSlash(configuredBaseUrl.trim()) + LOGGED_OUT_PATH;
        }
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(LOGGED_OUT_PATH)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    /**
     * Package-private so the shape of the URL can be asserted without a servlet container.
     */
    String logoutRedirectUrl(String loggedOutUrl) {

        // Appended by hand rather than through UriComponentsBuilder: its query encoding
        // leaves ':' and '/' intact, and the value has to be form-encoded for the identity
        // provider to match it against a registered service.
        //
        // Parsed only to reject a malformed value early. Any query string already configured
        // is kept — the provider may need parameters of its own alongside the service.
        String base = UriComponentsBuilder.fromUriString(sloUri).build().toUriString();
        char separator = base.indexOf('?') >= 0 ? '&' : '?';
        return base + separator + "service="
                + URLEncoder.encode(loggedOutUrl, StandardCharsets.UTF_8);
    }

    private static String stripTrailingSlash(String url) {

        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
    }
}
