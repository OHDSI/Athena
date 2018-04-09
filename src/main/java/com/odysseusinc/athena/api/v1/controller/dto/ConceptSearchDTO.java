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

package com.odysseusinc.athena.api.v1.controller.dto;

public class ConceptSearchDTO extends PageDTO {

    private String sort;
    private String order;

    private String query = "";

    private String[] vocabulary;
    private String[] domain;
    private String[] conceptClass;
    private String[] standardConcept;
    private String[] invalidReason;

    public String getSort() {

        return sort;
    }

    public void setSort(String sort) {

        this.sort = sort;
    }

    public String getOrder() {

        return order;
    }

    public void setOrder(String order) {

        this.order = order;
    }

    public String getQuery() {

        return query;
    }

    public void setQuery(String query) {

        this.query = query;
    }

    public String[] getVocabulary() {

        return vocabulary;
    }

    public void setVocabulary(String[] vocabulary) {

        this.vocabulary = vocabulary;
    }

    public String[] getDomain() {

        return domain;
    }

    public void setDomain(String[] domain) {

        this.domain = domain;
    }

    public String[] getConceptClass() {

        return conceptClass;
    }

    public void setConceptClass(String[] conceptClass) {

        this.conceptClass = conceptClass;
    }

    public String[] getStandardConcept() {

        return standardConcept;
    }

    public void setStandardConcept(String[] standardConcept) {

        this.standardConcept = standardConcept;
    }

    public String[] getInvalidReason() {

        return invalidReason;
    }

    public void setInvalidReason(String[] invalidReason) {

        this.invalidReason = invalidReason;
    }
}
