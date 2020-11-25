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

package com.odysseusinc.athena.api.v1.controller.dto;

import javax.validation.constraints.NotNull;

public class ConceptDTO {
    @NotNull
    private Long id;
    private String code;
    private String name;
    private String className;
    private String standardConcept;
    private String invalidReason;
    private String domain;
    private String vocabulary;
    private String score;

    private ConceptDTO() {

    }

    public static ConceptDTOBuilder builder() {

        return new ConceptDTO().new ConceptDTOBuilder();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

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

    public String getClassName() {

        return className;
    }

    public void setClassName(String className) {

        this.className = className;
    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {

        this.domain = domain;
    }

    public String getVocabulary() {

        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {

        this.vocabulary = vocabulary;
    }

    public String getStandardConcept() {

        return standardConcept;
    }

    public void setStandardConcept(String standardConcept) {

        this.standardConcept = standardConcept;
    }

    public String getInvalidReason() {

        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {

        this.invalidReason = invalidReason;
    }

    public String getScore() {

        return score;
    }

    public void setScore(String score) {

        this.score = score;
    }

    public class ConceptDTOBuilder {
        private Long id;
        private String code;
        private String name;
        private String className;
        private String standardConcept;
        private String invalidReason;
        private String domain;
        private String vocabulary;

        private ConceptDTOBuilder() {

        }

        public ConceptDTO build() {

            return ConceptDTO.this;
        }

        public ConceptDTOBuilder setId(Long id) {

            ConceptDTO.this.id = id;
            return this;
        }

        public ConceptDTOBuilder setCode(String code) {

            ConceptDTO.this.code = code;
            return this;
        }

        public ConceptDTOBuilder setName(String name) {

            ConceptDTO.this.name = name;
            return this;
        }

        public ConceptDTOBuilder setClassName(String className) {

            ConceptDTO.this.className = className;
            return this;
        }


        public ConceptDTOBuilder setStandardConcept(String standardConcept) {

            ConceptDTO.this.standardConcept = standardConcept;
            return this;
        }

        public ConceptDTOBuilder setInvalidReason(String invalidReason) {

            ConceptDTO.this.invalidReason = invalidReason;
            return this;
        }


        public ConceptDTOBuilder setDomain(String domain) {

            ConceptDTO.this.domain = domain;
            return this;
        }

        public ConceptDTOBuilder setVocabulary(String vocabulary) {

            ConceptDTO.this.vocabulary = vocabulary;
            return this;
        }
    }
}
