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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "concept_relationships_view")
@IdClass(ConceptRelationshipV5Id.class)
public class ConceptRelationship implements Serializable {

    @Id
    @Column(name = "source_concept_id")
    private Long sourceConceptId;

    @Column(name = "source_standard_concept")
    private String standard;
    @Id
    @Column(name = "target_concept_id")
    private Long targetConceptId;

    @Column(name = "target_concept_name")
    private String targetConceptName;

    @Column(name = "target_concept_vocabulary_id")
    private String targetConceptVocabularyId;
    @Id
    @Column(name = "relationship_id")
    private String relationshipId;

    @Column(name = "relationship_name")
    private String relationshipName;

    public Long getSourceConceptId() {

        return sourceConceptId;
    }

    public void setSourceConceptId(Long sourceConceptId) {

        this.sourceConceptId = sourceConceptId;
    }

    public Long getTargetConceptId() {

        return targetConceptId;
    }

    public void setTargetConceptId(Long targetConceptId) {

        this.targetConceptId = targetConceptId;
    }

    public String getTargetConceptName() {

        return targetConceptName;
    }

    public void setTargetConceptName(String targetConceptName) {

        this.targetConceptName = targetConceptName;
    }

    public String getTargetConceptVocabularyId() {

        return targetConceptVocabularyId;
    }

    public void setTargetConceptVocabularyId(String targetConceptVocabularyId) {

        this.targetConceptVocabularyId = targetConceptVocabularyId;
    }

    public String getRelationshipId() {

        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {

        this.relationshipId = relationshipId;
    }

    public String getRelationshipName() {

        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {

        this.relationshipName = relationshipName;
    }

    public String getStandard() {

        return standard;
    }

    public void setStandard(String standard) {

        this.standard = standard;
    }
}
