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

package com.odysseusinc.athena.model.athenav4;

import com.odysseusinc.athena.model.common.EntityV4;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Entity
@IdClass(ConceptRelationshipV4Id.class)
@Table(name = "concept_relationship")
public class ConceptRelationshipV4 extends EntityV4 {

    @Id
    @NotNull
    @ManyToOne(optional = false, targetEntity = ConceptV4.class)
    @JoinColumn(name = "concept_id_1", referencedColumnName = "concept_id",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private ConceptV4 sourceConcept;

    @Id
    @NotNull
    @ManyToOne(optional = false, targetEntity = ConceptV4.class)
    @JoinColumn(name = "concept_id_2", referencedColumnName = "concept_id",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private ConceptV4 targetConcept;

    @Id
    @NotNull
    @ManyToOne(optional = false, targetEntity = RelationshipV4.class)
    @JoinColumn(name = "relationship_id", referencedColumnName = "relationship_id",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private RelationshipV4 relationship;

    @Id
    @NotBlank
    @Column(name = "valid_start_date")
    private Date validStart;

    @Id
    @NotBlank
    @Column(name = "valid_end_date")
    private Date validEnd;

    @Column(name = "invalid_reason")
    private String invalidReason;

    public ConceptV4 getSourceConcept() {

        return sourceConcept;
    }

    public void setSourceConcept(ConceptV4 sourceConcept) {

        this.sourceConcept = sourceConcept;
    }

    public ConceptV4 getTargetConcept() {

        return targetConcept;
    }

    public void setTargetConcept(ConceptV4 targetConcept) {

        this.targetConcept = targetConcept;
    }

    public RelationshipV4 getRelationship() {

        return relationship;
    }

    public void setRelationship(RelationshipV4 relationship) {

        this.relationship = relationship;
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
}
