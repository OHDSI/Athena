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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * The {@link RestTemplate} used to call the Arachne portal (registration, password
 * remind/reset, professional types, countries, provinces).
 *
 * <h2>TLS verification is now enabled — behaviour change</h2>
 * This class previously built an Apache HttpClient 4 with a trust-all
 * {@code X509TrustManager} <em>and</em> called
 * {@code HttpsURLConnection.setDefaultSSLSocketFactory(...)}, which disabled certificate
 * validation for the entire JVM rather than just this client. Spring Boot 4
 * ships Apache HttpClient 5, whose API differs, so no mechanical port existed — the
 * choice was to re-author the trust-all behaviour against the new API or drop it.
 * <p>
 * It is dropped: the default factory validates certificates. That closes, but it
 * is a genuine behaviour change. <b>If the Arachne portal presents a self-signed or
 * otherwise untrusted certificate these calls will now fail</b> where they previously
 * succeeded. The fix in that case is to add the portal's CA to the JVM truststore, not to
 * reinstate a trust-all manager.
 */
@Configuration
public class IntegrationConfig {

    @Bean
    public RestTemplate centralRestTemplate() {

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        return restTemplate;
    }
}
