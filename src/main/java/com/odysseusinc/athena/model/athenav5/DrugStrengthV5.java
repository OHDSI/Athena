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

import com.odysseusinc.athena.model.common.DrugStrengthPK;
import com.odysseusinc.athena.model.common.EntityV5;
import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "drug_strength")
@IdClass(DrugStrengthPK.class)
public class DrugStrengthV5 extends EntityV5 implements Serializable {

    @Id
    @NotBlank
    @Column(name = "drug_concept_id")
    private Long id;

    @NotBlank
    @Id
    @Column(name = "ingredient_concept_id")
    private Long ingredientId;

    @Column(name = "amount_value")
    private Float amountValue;

    @Column(name = "amount_unit_concept_id")
    private Integer amountUnitId;

    @Column(name = "numerator_value")
    private Float numeratorValue;

    @Column(name = "numerator_unit_concept_id")
    private Integer numeratorUnitId;

    @Column(name = "denominator_value")
    private Float denominatorValue;

    @Column(name = "denominator_unit_concept_id")
    private Integer denominatorUnitId;

    @NotBlank
    @Column(name = "valid_start_date")
    private Date validStart;

    @NotBlank
    @Column(name = "valid_end_date")
    private Date validEnd;

    @Column(name = "invalid_reason")
    private String invalidReason;

    @Column(name = "box_size")
    private Integer boxSize;

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

    public Float getAmountValue() {

        return amountValue;
    }

    public void setAmountValue(Float amountValue) {

        this.amountValue = amountValue;
    }

    public Integer getAmountUnitId() {

        return amountUnitId;
    }

    public void setAmountUnitId(Integer amountUnitId) {

        this.amountUnitId = amountUnitId;
    }

    public Float getNumeratorValue() {

        return numeratorValue;
    }

    public void setNumeratorValue(Float numeratorValue) {

        this.numeratorValue = numeratorValue;
    }

    public Integer getNumeratorUnitId() {

        return numeratorUnitId;
    }

    public void setNumeratorUnitId(Integer numeratorUnitId) {

        this.numeratorUnitId = numeratorUnitId;
    }

    public Float getDenominatorValue() {

        return denominatorValue;
    }

    public void setDenominatorValue(Float denominatorValue) {

        this.denominatorValue = denominatorValue;
    }

    public Integer getDenominatorUnitId() {

        return denominatorUnitId;
    }

    public void setDenominatorUnitId(Integer denominatorUnitId) {

        this.denominatorUnitId = denominatorUnitId;
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

    public Integer getBoxSize() {

        return boxSize;
    }

    public void setBoxSize(Integer boxSize) {

        this.boxSize = boxSize;
    }
}
