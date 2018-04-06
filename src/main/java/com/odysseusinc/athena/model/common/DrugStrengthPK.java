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

package com.odysseusinc.athena.model.common;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.IdClass;

@IdClass(DrugStrengthPK.class)
public class DrugStrengthPK implements Serializable {

    private Long id;
    private Long ingredientId;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getIngredientId() {

        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {

        this.ingredientId = ingredientId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DrugStrengthPK that = (DrugStrengthPK) obj;
        return Objects.equals(id, that.id)
                && Objects.equals(ingredientId, that.ingredientId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, ingredientId);
    }
}
