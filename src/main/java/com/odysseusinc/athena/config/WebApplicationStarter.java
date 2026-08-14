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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Boot 4 splits auto-configuration into per-module artifacts. spring-boot-flyway is not
// a dependency here (this application runs Flyway itself, across four datasources, from
// FlywayConfig), so FlywayAutoConfiguration is absent and no longer needs excluding.
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@Configuration
@EnableScheduling
@EnableRetry
@EnableAsync
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.odysseusinc.athena.api.v1.controller",
        "com.odysseusinc.athena.config",
        // This list predates the pac4j removal, when everything security-related was
        // wired explicitly from Pac4jConfig inside `config`. The replacement puts the JWT,
        // API-token, HMAC and SAML beans under `security`, and without this entry none of the
        // eight is registered - the context fails on the first one it needs. The Cucumber
        // suite does not catch it: it builds its context from TestConfiguration, not from
        // this class.
        "com.odysseusinc.athena.security.**",
        "com.odysseusinc.athena.service.**",
        "com.odysseusinc.athena.repositories.*",
        "com.odysseusinc.athena.controllers"})
public class WebApplicationStarter extends SpringBootServletInitializer {
    public static void main(String[] args) {

        new SpringApplication(WebApplicationStarter.class).run(args);
    }
}
