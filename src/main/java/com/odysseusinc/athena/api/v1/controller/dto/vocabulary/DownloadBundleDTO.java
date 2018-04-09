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

package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import com.odysseusinc.athena.util.DownloadBundleStatus;
import java.util.Date;
import java.util.List;

public class DownloadBundleDTO {

    private Long id;
    private Date date;
    private String link;
    private String name;
    private Float cdmVersion;
    private DownloadBundleStatus status;

    private List<VocabularyDTO> vocabularies;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }

    public String getLink() {

        return link;
    }

    public void setLink(String link) {

        this.link = link;
    }

    public List<VocabularyDTO> getVocabularies() {

        return vocabularies;
    }

    public void setVocabularies(List<VocabularyDTO> vocabularies) {

        this.vocabularies = vocabularies;
    }

    public Float getCdmVersion() {

        return cdmVersion;
    }

    public void setCdmVersion(Float cdmVersion) {

        this.cdmVersion = cdmVersion;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public DownloadBundleStatus getStatus() {

        return status;
    }

    public void setStatus(DownloadBundleStatus status) {

        this.status = status;
    }
}
