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

import com.odysseusinc.athena.service.security.RevokedTokenStore;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.springframework.security.web.CallbackFilter;
import org.pac4j.springframework.security.web.LogoutFilter;
import org.pac4j.springframework.security.web.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Configuration
    @Order(1)
    public static class Saml2WebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Config config;

        protected void configure(final HttpSecurity http) throws Exception {

            final SecurityFilter filter = new SecurityFilter(config, "Saml2Client");

            http
                    .antMatcher("/auth/sso")
                    .addFilterBefore(filter, BasicAuthenticationFilter.class)
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        }
    }

    @Configuration
    @Order(2)
    public static class JwtWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Config config;
        @Autowired
        private RevokedTokenStore tokenStore;

        @Value("${athena.token.header}")
        private String authTokenHeader;

        @Override
        public void configure(WebSecurity webSecurity) throws Exception {

            webSecurity
                    .ignoring()
                    .antMatchers("/api/v1/users**")

                    .antMatchers("/save")
                    .antMatchers("/downloads")
                    .antMatchers("licenses/suggest")
                    .antMatchers("licenses")
                    .antMatchers("licenses/{id}")
                    .antMatchers("licenses/request")
                    .antMatchers("licenses/accept")
                    .antMatchers("/admin/licenses**")
                    .antMatchers("/admin/statistics**")

                    .antMatchers("/api/v1/users/remind-password**")
                    .antMatchers("/api/v1/users/reset-password**")
                    .antMatchers("/api/v1/users/professional-types**")
                    .antMatchers("/api/v1/vocabularies/licenses/accept/mail**")
                    .antMatchers("/api/v1/vocabularies/releaseVersion")
                    .antMatchers("/api/v1/vocabularies/zip/**")
                    .antMatchers("/api/v1/build-number")
                    .antMatchers("/app.*.js", "/fonts/**", "/icons/**");
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {

            final SecurityFilter filter = new SecurityFilter(config, "HeaderClient,AnonymousClient");

            http
                    .antMatcher("/api/**")
                    .addFilterBefore(filter, BasicAuthenticationFilter.class)
                    .csrf()
                    .disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        }

        @Bean
        public FilterRegistrationBean logoutFilterRegistration() {

            FilterRegistrationBean bean = new FilterRegistrationBean();
            bean.setFilter(logoutFilter());
            bean.addUrlPatterns("/api/v1/users/logout");
            bean.setName("logoutFilter");
            bean.setOrder(1);
            return bean;
        }

        @Bean
        public LogoutFilter logoutFilter() {

            final LogoutFilter logoutFilter = new LogoutFilter(config);
            logoutFilter.setLogoutLogic(new CustomLogoutLogic<>(tokenStore));
            logoutFilter.setLocalLogout(true);
            logoutFilter.setDestroySession(false);
            logoutFilter.setCentralLogout(true);
            return logoutFilter;
        }
    }

    @Configuration
    @Order(3)
    public static class DefaultWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Config config;

        @Autowired
        private JwtGenerator<CommonProfile> jwtGenerator;

        protected void configure(final HttpSecurity http) throws Exception {

            final CallbackFilter asyncSecurityCallbackFilter = new CallbackFilter(config);
            asyncSecurityCallbackFilter.setMultiProfile(true);
            asyncSecurityCallbackFilter.setCallbackLogic(customPac4jCallbackLogic(jwtGenerator));

            http
                    .csrf()
                    .disable()
                    .exceptionHandling()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            http
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "*/**").permitAll()
                    .antMatchers("/").permitAll()
                    .antMatchers("/**.js").permitAll()
                    .antMatchers("/error").permitAll()
                    .antMatchers("/index.html**").permitAll()
                    .antMatchers("/fonts*/**").permitAll()
                    .antMatchers("/img*/**").permitAll()
                    .antMatchers("/swagger-ui.html*/**").permitAll()
                    .antMatchers("/webjars*/**").permitAll()
                    .antMatchers("/swagger-resources*/**").permitAll()
                    .antMatchers("/configuration*/**").permitAll()
                    .antMatchers("/api/v1/build-number*/**").permitAll()
                    .antMatchers("/auth/saml-metadata").permitAll()
                    .antMatchers("/api/v1/concepts**").permitAll()

                    .and()
                    .addFilterBefore(asyncSecurityCallbackFilter, BasicAuthenticationFilter.class)
                    .logout()
                    .logoutSuccessUrl("/");
        }

        @Bean
        public CustomPac4jCallbackLogic customPac4jCallbackLogic(JwtGenerator jwtGenerator) {

            return new CustomPac4jCallbackLogic(jwtGenerator);
        }
    }


}
