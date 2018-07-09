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
import java.util.Objects;


public class ConceptRelationshipV5Id implements Serializable {

    private Long sourceConceptId;
    private Long targetConceptId;
    private String relationshipId;

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

    public String getRelationshipId() {

        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {

        this.relationshipId = relationshipId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConceptRelationshipV5Id that = (ConceptRelationshipV5Id) obj;
        return Objects.equals(sourceConceptId, that.sourceConceptId)
                && Objects.equals(targetConceptId, that.targetConceptId)
                && Objects.equals(relationshipId, that.relationshipId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sourceConceptId, targetConceptId, relationshipId);
    }
}
