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

import java.util.Date;

public class VocabularyDTO {
    private Integer id;
    private String code;
    private String name;
    private String required;
    private Date update;
    private boolean clickDefault;
    private boolean omopReq;
    private String url;
    private Date expiredDate;
    private String statusLicense;

    public VocabularyDTO() {

    }

    public VocabularyDTO(VocabularyDTO other) {

        this.id = other.id;
        this.code = other.code;
        this.name = other.name;
        this.required = other.required;
        this.update = other.update;
        this.clickDefault = other.clickDefault;
        this.omopReq = other.omopReq;
        this.url = other.url;
        this.expiredDate = other.expiredDate;
        this.statusLicense = other.statusLicense;
    }

    public static VocabularyDTO.VocabularyDTOBuilder builder() {

        return new VocabularyDTO().new VocabularyDTOBuilder();
    }

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getRequired() {

        return required;
    }

    public void setRequired(String required) {

        this.required = required;
    }

    public Date getUpdate() {

        return update;
    }

    public void setUpdate(Date update) {

        this.update = update;
    }

    public boolean isClickDefault() {

        return clickDefault;
    }

    public void setClickDefault(boolean clickDefault) {

        this.clickDefault = clickDefault;
    }

    public boolean isOmopReq() {

        return omopReq;
    }

    public void setOmopReq(boolean omopReq) {

        this.omopReq = omopReq;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public Date getExpiredDate() {

        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {

        this.expiredDate = expiredDate;
    }
    public String getStatusLicense() {

        return statusLicense;
    }

    public void setStatusLicense(String statusLicense) {

        this.statusLicense = statusLicense;
    }

    public class VocabularyDTOBuilder {
        private Integer id;
        private String code;
        private String name;
        private String required;
        private Date update;
        private boolean clickDefault;
        private boolean omopReq;
        private String url;
        private Date expiredDate;
        private String statusLicense;


        private VocabularyDTOBuilder() {

        }

        public VocabularyDTO build() {

            return VocabularyDTO.this;
        }

        public VocabularyDTOBuilder setId(Integer id) {


            VocabularyDTO.this.id = id;
            return this;
        }

        public VocabularyDTOBuilder setCode(String code) {


            VocabularyDTO.this.code = code;
            return this;
        }

        public VocabularyDTOBuilder setName(String name) {


            VocabularyDTO.this.name = name;
            return this;
        }

        public VocabularyDTOBuilder setRequired(String required) {


            VocabularyDTO.this.required = required;
            return this;
        }

        public VocabularyDTOBuilder setUpdate(Date update) {


            VocabularyDTO.this.update = update;
            return this;
        }

        public VocabularyDTOBuilder setClickDefault(boolean clickDefault) {

            VocabularyDTO.this.clickDefault = clickDefault;
            return this;
        }

        public VocabularyDTOBuilder setOmopReq(boolean omopReq) {

            VocabularyDTO.this.omopReq = omopReq;
            return this;
        }

        public VocabularyDTOBuilder setUrl(String url) {

            VocabularyDTO.this.url = url;
            return this;
        }

        public VocabularyDTOBuilder setExpiredDate(Date expiredDate) {

            VocabularyDTO.this.expiredDate = expiredDate;
            return this;
        }

        public VocabularyDTOBuilder setStatusLicense(String statusLicense) {

            VocabularyDTO.this.statusLicense = statusLicense;
            return this;
        }

    }

}
