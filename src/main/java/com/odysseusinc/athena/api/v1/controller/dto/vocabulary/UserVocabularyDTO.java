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

package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import com.odysseusinc.athena.util.extractor.LicenseStatus;

import java.util.Date;


public class UserVocabularyDTO extends VocabularyDTO {

    private boolean available;
    private Long licenseId;
    private Date requestDate;
    private LicenseStatus status;
    private String token;

    public UserVocabularyDTO(VocabularyDTO vocabularyDTO) {

        super(vocabularyDTO);
    }

    public boolean isAvailable() {

        return available;
    }

    public void setAvailable(boolean available) {

        this.available = available;
    }

    public Long getLicenseId() {

        return licenseId;
    }

    public void setLicenseId(Long licenseId) {

        this.licenseId = licenseId;
    }

    public LicenseStatus getStatus() {

        return status;
    }

    public void setStatus(LicenseStatus status) {

        this.status = status;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public Date getRequestDate() {

        return requestDate;
    }

    public void setRequestDate(Date requestDate) {

        this.requestDate = requestDate;
    }
}
