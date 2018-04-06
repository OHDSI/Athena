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

package com.odysseusinc.athena.api.v1.controller.converter;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {
    @Value("${athena.url}")
    private String athenaUrl;

    public String downloadVocabulariesLink(String uuid) {

        return athenaUrl + "/api/v1/vocabularies/zip/" + uuid;
    }

    public String acceptLicenseRequestLink(Long id, Boolean accept, String uuid) {

        return format("%s/api/v1/vocabularies/licenses/accept/mail?id=%s&accepted=%s&token=%s",
                athenaUrl, id, accept, uuid);
    }

    public String downloadVocabulariesPageUrl() {

        return athenaUrl + "/vocabulary/list";
    }

}
