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

package com.odysseusinc.athena.service.graph;

import com.google.common.base.Objects;

public class RelationGraphParameter {
    private Long id;
    private Integer depth;

    public RelationGraphParameter() {

    }

    public RelationGraphParameter(Long id, Integer depth) {

        this.id = id;
        this.depth = depth;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Integer getDepth() {

        return depth;
    }

    public void setDepth(Integer depth) {

        this.depth = depth;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationGraphParameter that = (RelationGraphParameter) o;
        return Objects.equal(id, that.id)
                && Objects.equal(depth, that.depth);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(id, depth);
    }
}
