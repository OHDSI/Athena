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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "relationship")
public class RelationshipV4 extends EntityV4 {

    @Id
    @NotBlank
    @Column(name = "relationship_id")
    private Integer id;

    @NotBlank
    @Column(name = "relationship_name")
    private String name;

    @NotBlank
    @Column(name = "is_hierarchical")
    private Integer isHierarchical;

    @NotBlank
    @Column(name = "defines_ancestry")
    private Integer definesAncestry;

    @Column(name = "reverse_relationship")
    private Integer reverseId;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Integer getIsHierarchical() {

        return isHierarchical;
    }

    public void setIsHierarchical(Integer isHierarchical) {

        this.isHierarchical = isHierarchical;
    }

    public Integer getDefinesAncestry() {

        return definesAncestry;
    }

    public void setDefinesAncestry(Integer definesAncestry) {

        this.definesAncestry = definesAncestry;
    }

    public Integer getReverseId() {

        return reverseId;
    }

    public void setReverseId(Integer reverseId) {

        this.reverseId = reverseId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RelationshipV4 that = (RelationshipV4) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
