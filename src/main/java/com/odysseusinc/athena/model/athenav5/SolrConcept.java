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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "concepts_view")
public class SolrConcept extends EntityV5 {
    @Id
    @NotBlank
    @Column(name = "concept_id")
    private Long id;

    @NotNull
    @Column(name = "concept_name")
    private String name;

    @NotNull
    @ManyToOne(optional = false, targetEntity = DomainV5.class)
    @JoinColumn(name = "domain_id", referencedColumnName = "domain_id")
    private DomainV5 domain;

    @NotNull
    @ManyToOne(optional = false, targetEntity = VocabularyV5.class)
    @JoinColumn(name = "vocabulary_id", referencedColumnName = "vocabulary_id")
    private VocabularyV5 vocabulary;

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

    // Hibernate 6 removed @LazyCollection; LazyCollectionOption.FALSE meant eager,
    // so the equivalent is fetch = EAGER on the association itself.
    @OneToMany(mappedBy = "id", targetEntity = ConceptSynonymV5.class, fetch = FetchType.EAGER)
    private List<ConceptSynonymV5> synonyms;

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

    public DomainV5 getDomain() {

        return domain;
    }

    public void setDomain(DomainV5 domain) {

        this.domain = domain;
    }

    public VocabularyV5 getVocabulary() {

        return vocabulary;
    }

    public void setVocabulary(VocabularyV5 vocabulary) {

        this.vocabulary = vocabulary;
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

        return domain != null ? domain.getId() : StringUtils.EMPTY;
    }

    public String getVocabularyId() {

        return vocabulary != null ? vocabulary.getId() : StringUtils.EMPTY;
    }

    public List<ConceptSynonymV5> getSynonyms() {

        return synonyms;
    }

    public void setSynonyms(List<ConceptSynonymV5> synonyms) {

        this.synonyms = synonyms;
    }
}
