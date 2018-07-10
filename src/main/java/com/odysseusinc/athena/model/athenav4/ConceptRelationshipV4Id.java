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

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.IdClass;

@IdClass(ConceptRelationshipV4Id.class)
public class ConceptRelationshipV4Id implements Serializable {

    private ConceptV4 sourceConcept;
    private ConceptV4 targetConcept;
    private RelationshipV4 relationship;
    private Date validStart;
    private Date validEnd;

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

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConceptRelationshipV4Id that = (ConceptRelationshipV4Id) obj;
        return Objects.equals(sourceConcept, that.sourceConcept)
                && Objects.equals(targetConcept, that.targetConcept)
                && Objects.equals(relationship, that.relationship)
                && Objects.equals(validStart, that.validStart)
                && Objects.equals(validEnd, that.validEnd);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sourceConcept, targetConcept, relationship, validStart, validEnd);
    }
}
