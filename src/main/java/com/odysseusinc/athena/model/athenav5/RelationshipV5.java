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
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "relationship")
public class RelationshipV5 extends EntityV5 {

    @Id
    @NotBlank
    @Column(name = "relationship_id")
    private String id;

    @NotBlank
    @Column(name = "relationship_name")
    private String name;

    @NotBlank
    @Column(name = "is_hierarchical")
    private String isHierarchical;

    @NotBlank
    @Column(name = "defines_ancestry")
    private String definesAncestry;

    @NotBlank
    @Column(name = "reverse_relationship_id")
    private String reverseId;

    @NotBlank
    @Column(name = "relationship_concept_id")
    private BigInteger conceptId;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIsHierarchical() {

        return isHierarchical;
    }

    public void setIsHierarchical(String isHierarchical) {

        this.isHierarchical = isHierarchical;
    }

    public String getDefinesAncestry() {

        return definesAncestry;
    }

    public void setDefinesAncestry(String definesAncestry) {

        this.definesAncestry = definesAncestry;
    }

    public String getReverseId() {

        return reverseId;
    }

    public void setReverseId(String reverseId) {

        this.reverseId = reverseId;
    }

    public BigInteger getConceptId() {

        return conceptId;
    }

    public void setConceptId(BigInteger conceptId) {

        this.conceptId = conceptId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RelationshipV5 that = (RelationshipV5) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
