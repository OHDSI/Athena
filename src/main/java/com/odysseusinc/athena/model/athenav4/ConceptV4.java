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

package com.odysseusinc.athena.model.athenav4;

import com.odysseusinc.athena.model.common.EntityV4;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "concept")
public class ConceptV4 extends EntityV4 {
    @Id
    @NotBlank
    @Column(name = "concept_id")
    private Long id;

    @NotNull
    @Column(name = "concept_name")
    private String name;

    @Column(name = "concept_level")
    private Integer conceptLevel;

    @Column(name = "vocabulary_id")
    private Long vocabularyId;

    @NotNull
    @Column(name = "concept_class")
    private String conceptClass;

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

    public Integer getConceptLevel() {

        return conceptLevel;
    }

    public void setConceptLevel(Integer conceptLevel) {

        this.conceptLevel = conceptLevel;
    }

    public String getConceptClass() {

        return conceptClass;
    }

    public void setConceptClass(String conceptClass) {

        this.conceptClass = conceptClass;
    }

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

    public Long getVocabularyId() {

        return vocabularyId;
    }

    public void setVocabularyId(Long vocabularyId) {

        this.vocabularyId = vocabularyId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConceptV4 conceptV4 = (ConceptV4) obj;
        return Objects.equals(id, conceptV4.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
