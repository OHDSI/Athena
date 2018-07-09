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

package com.odysseusinc.athena.model.athenav5;

import com.odysseusinc.athena.model.common.EntityV5;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "concept")
public class ConceptV5 extends EntityV5 {
    @Id
    @NotBlank
    @Column(name = "concept_id")
    private Long id;

    @NotNull
    @Column(name = "concept_name")
    private String name;

    @Column(name = "domain_id")
    private String domainId;

    @NotNull
    @ManyToOne(optional = false, targetEntity = VocabularyV5.class)
    @JoinColumn(name = "vocabulary_id", referencedColumnName = "vocabulary_id")
    private VocabularyV5 vocabulary;

    @OneToMany(mappedBy = "id", targetEntity = ConceptSynonymV5.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ConceptSynonymV5> synonyms;

    @NotNull
    @Column(name = "concept_class_id")
    private String conceptClassId;

    @Column(name = "standard_concept")
    private String standardConcept;

    @NotNull
    @Column(name = "concept_code")
    private String conceptCode;

    @NotNull
    @Column(name = "valid_start_date")
    private Date validStart;

    @NotNull
    @Column(name = "valid_end_date")
    private Date validEnd;

    @Column(name = "invalid_reason")
    private String invalidReason;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getConceptClassId() {

        return conceptClassId;
    }

    public void setConceptClassId(String conceptClassId) {

        this.conceptClassId = conceptClassId;
    }

    public String getStandardConcept() {

        return standardConcept;
    }

    public void setStandardConcept(String standardConcept) {

        this.standardConcept = standardConcept;
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

    public String getInvalidReason() {

        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {

        this.invalidReason = invalidReason;
    }

    public String getDomainId() {

        return domainId;
    }

    public void setDomainId(String domainId) {

        this.domainId = domainId;
    }

    public VocabularyV5 getVocabulary() {

        return vocabulary;
    }

    public String getVocabularyId() {

        return vocabulary.getId();
    }

    public void setVocabulary(VocabularyV5 vocabulary) {

        this.vocabulary = vocabulary;
    }

    public List<ConceptSynonymV5> getSynonyms() {

        return synonyms;
    }

    public void setSynonyms(List<ConceptSynonymV5> synonyms) {

        this.synonyms = synonyms;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConceptV5 conceptV5 = (ConceptV5) obj;
        return Objects.equals(id, conceptV5.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
