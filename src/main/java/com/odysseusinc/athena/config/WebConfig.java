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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import static com.odysseusinc.athena.model.common.AthenaConstants.DEFAULT_TEMPLATE_NAME;

/**
 * {@code @EnableWebMvc} was removed here.
 * <p>
 * It switches off Spring Boot's MVC auto-configuration wholesale — error handling, content
 * negotiation, message converters and static resource defaults — leaving this class to
 * re-supply them. Implementing {@link WebMvcConfigurer} without the annotation keeps every
 * customisation below while letting Boot's defaults apply underneath.
 * <p>
 * It was also masking a latent break. Boot 2.6 switched MVC path matching to
 * {@code PathPatternParser}, which rejects {@code **} unless it is a complete trailing
 * segment; {@code @EnableWebMvc} suppressed that property, so patterns like
 * {@code "/admin/licenses**"} kept working under the old {@code AntPathMatcher}. Boot 3
 * removed {@code AntPathMatcher} entirely, so those patterns are rewritten below — without
 * that, removing this annotation fails at startup with {@code PatternParseException}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${athena.messages.cache_seconds:3600}")
    private int messagesCacheSeconds;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**").addResourceLocations("classpath:/public/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public MessageSource messageSource() {

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setCacheSeconds(messagesCacheSeconds); //refresh cache once per hour
        return messageSource;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/index.html").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/register").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/login").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/complete").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/reset-password/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/remind-password").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/auth/remind-password/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/search-terms/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/vocabulary/list").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/vocabulary/list/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/vocabulary/download-history").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/vocabulary/download-history/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/admin/licenses").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/admin/licenses/**").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/admin/statistics").setViewName(DEFAULT_TEMPLATE_NAME);
        registry.addViewController("/admin/statistics/**").setViewName(DEFAULT_TEMPLATE_NAME);
    }

    @Bean
    public LocaleResolver localeResolver() {

        return new CookieLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {

        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public Validator getValidator() {

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }

}
