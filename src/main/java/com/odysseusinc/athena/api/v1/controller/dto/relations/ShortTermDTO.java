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

package com.odysseusinc.athena.api.v1.controller.dto.relations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class ShortTermDTO {
    protected Long id;
    protected String name;
    @JsonProperty(value = "isCurrent")
    private Boolean current;
    protected Integer weight;
    private Integer depth;

    private Integer count = 1;

    public ShortTermDTO() {

    }

    public ShortTermDTO(ShortTermDTO other) {

        this.id = other.id;
        this.name = other.name;
        this.current = other.current;
        this.weight = other.weight;
        this.depth = other.depth;
        this.count = other.count;
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

    public Boolean getCurrent() {

        return current;
    }

    public void setCurrent(Boolean current) {

        this.current = current;
    }

    public Integer getWeight() {

        return weight;
    }

    public void setWeight(Integer weight) {

        this.weight = weight;
    }

    public Integer getDepth() {

        return depth;
    }

    public void setDepth(Integer depth) {

        this.depth = depth;
    }

    public Integer getCount() {

        return count;
    }

    public void setCount(Integer count) {

        this.count = count;
    }

    @Override
    public boolean equals(Object another) {

        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        ShortTermDTO dto = (ShortTermDTO) another;
        return Objects.equal(id, dto.id)
                && Objects.equal(weight, dto.weight);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(id, weight);
    }
}
