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

package com.odysseusinc.athena.model.athenav5;

import com.odysseusinc.athena.model.common.EntityV5;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.validation.constraints.NotNull;

@Entity
@IdClass(ConceptAncestorRelationId.class)
public class ConceptAncestorRelationV5 extends EntityV5 implements Serializable {
    @Column(name = "concept_id")
    private Long id;
    @NotNull
    @Column(name = "concept_name")
    private String name;
    @NotNull
    @Column(name = "concept_class_id")
    private String conceptClassId;
    @NotNull
    @Column(name = "vocabulary_id")
    private String vocabularyId;
    @Id
    @Column(name = "ancestor_concept_id")
    private Long ancestorId;
    @Id
    @Column(name = "descendant_concept_id")
    private Long descendantId;
    @Column(name = "weight")
    private Integer weight;
    @Column(name = "is_current")
    private Boolean current;
    @Column
    private Integer depth;

    public ConceptAncestorRelationV5() {
    }

    public ConceptAncestorRelationV5(Long id, String name, String conceptClassId, String vocabularyId, Long ancestorId,
                                     Long descendantId, Integer weight, Boolean current, Integer depth) {

        this.id = id;
        this.name = name;
        this.conceptClassId = conceptClassId;
        this.vocabularyId = vocabularyId;
        this.ancestorId = ancestorId;
        this.descendantId = descendantId;
        this.weight = weight;
        this.current = current;
        this.depth = depth;
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

    public String getConceptClassId() {

        return conceptClassId;
    }

    public void setConceptClassId(String conceptClassId) {

        this.conceptClassId = conceptClassId;
    }

    public String getVocabularyId() {

        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {

        this.vocabularyId = vocabularyId;
    }

    public Long getAncestorId() {

        return ancestorId;
    }

    public void setAncestorId(Long ancestorId) {

        this.ancestorId = ancestorId;
    }

    public Long getDescendantId() {

        return descendantId;
    }

    public void setDescendantId(Long descendantId) {

        this.descendantId = descendantId;
    }

    public Integer getWeight() {

        return weight;
    }

    public void setWeight(Integer weight) {

        this.weight = weight;
    }

    public Boolean getCurrent() {

        return current;
    }

    public void setCurrent(Boolean current) {

        this.current = current;
    }

    public Integer getDepth() {

        return depth;
    }

    public void setDepth(Integer depth) {

        this.depth = depth;
    }
}
