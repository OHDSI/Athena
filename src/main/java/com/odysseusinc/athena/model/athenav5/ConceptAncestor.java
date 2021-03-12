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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "concept_ancestor")
public class ConceptAncestor extends EntityV5 {

    @Id
    @NotBlank
    @Column(name = "ancestor_concept_id")
    private Long id;

    @NotBlank
    @ManyToOne(optional = false, targetEntity = SolrConcept.class)
    @JoinColumn(name = "descendant_concept_id", referencedColumnName = "concept_id",
            insertable = false, updatable = false)
    private SolrConcept descendantConcept;

    @NotBlank
    @Column(name = "min_levels_of_separation")
    private Integer minLevelsOfSeparation;

    @NotBlank
    @Column(name = "max_levels_of_separation")
    private Integer maxLevelsOfSeparation;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Integer getMinLevelsOfSeparation() {

        return minLevelsOfSeparation;
    }

    public void setMinLevelsOfSeparation(Integer minLevelsOfSeparation) {

        this.minLevelsOfSeparation = minLevelsOfSeparation;
    }

    public Integer getMaxLevelsOfSeparation() {

        return maxLevelsOfSeparation;
    }

    public void setMaxLevelsOfSeparation(Integer maxLevelsOfSeparation) {

        this.maxLevelsOfSeparation = maxLevelsOfSeparation;
    }

    public SolrConcept getDescendantConcept() {
        return descendantConcept;
    }

    public void setDescendantConcept(SolrConcept descendantConcept) {
        this.descendantConcept = descendantConcept;
    }

}
