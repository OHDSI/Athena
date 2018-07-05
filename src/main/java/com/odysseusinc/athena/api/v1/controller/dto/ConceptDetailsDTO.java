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

import java.util.Date;
import java.util.List;

public class ConceptDetailsDTO {

    private Long id;
    private String name;
    private String domainId;
    private String conceptClassId;
    private String vocabularyId;
    private String standardConcept;
    private String conceptCode;

    private Date validStart;
    private Date validEnd;

    private String invalidReason;
    private List<String> synonyms;

    private ShortConceptDTO validTerm;

    private String vocabularyName;
    private String vocabularyVersion;
    private String vocabularyReference;

    public ConceptDetailsDTO() {

    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getConceptClassId() {

        return conceptClassId;
    }

    public void setConceptClassId(String conceptClassId) {

        this.conceptClassId = conceptClassId;
    }

    public String getConceptCode() {

        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {

        this.conceptCode = conceptCode;
    }

    public Date getValidStart() {

        return validStart;
    }

    public void setValidStart(Date validStart) {

        this.validStart = validStart;
    }

    public Date getValidEnd() {

        return validEnd;
    }

    public void setValidEnd(Date validEnd) {

        this.validEnd = validEnd;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDomainId() {

        return domainId;
    }

    public void setDomainId(String domainId) {

        this.domainId = domainId;
    }

    public String getVocabularyId() {

        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {

        this.vocabularyId = vocabularyId;
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

    public List<String> getSynonyms() {

        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {

        this.synonyms = synonyms;
    }

    public ShortConceptDTO getValidTerm() {

        return validTerm;
    }

    public void setValidTerm(ShortConceptDTO validTerm) {

        this.validTerm = validTerm;
    }

    public String getVocabularyName() {

        return vocabularyName;
    }

    public void setVocabularyName(String vocabularyName) {

        this.vocabularyName = vocabularyName;
    }

    public String getVocabularyVersion() {

        return vocabularyVersion;
    }

    public void setVocabularyVersion(String vocabularyVersion) {

        this.vocabularyVersion = vocabularyVersion;
    }

    public String getVocabularyReference() {

        return vocabularyReference;
    }

    public void setVocabularyReference(String vocabularyReference) {

        this.vocabularyReference = vocabularyReference;
    }
}
