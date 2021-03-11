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

import com.odysseusinc.athena.model.common.DrugStrengthPK;
import com.odysseusinc.athena.model.common.EntityV4;
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
public class DrugStrengthV4 extends EntityV4 implements Serializable {

    @Id
    @NotBlank
    @Column(name = "drug_concept_id")
    private Long id;

    @Id
    @NotBlank
    @Column(name = "ingredient_concept_id")
    private Long ingredientId;

    @Column(name = "amount_value")
    private Float amountValue;

    @Column(name = "amount_unit")
    private String amountUnit;

    @Column(name = "concentration_value")
    private Float concentrationValue;

    @Column(name = "concentration_enum_unit")
    private String concentrationEnumUnit;

    @Column(name = "concentration_denom_unit")
    private String concentrationDenomUnit;

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

    public String getAmountUnit() {

        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {

        this.amountUnit = amountUnit;
    }

    public Float getConcentrationValue() {

        return concentrationValue;
    }

    public void setConcentrationValue(Float concentrationValue) {

        this.concentrationValue = concentrationValue;
    }

    public String getConcentrationEnumUnit() {

        return concentrationEnumUnit;
    }

    public void setConcentrationEnumUnit(String concentrationEnumUnit) {

        this.concentrationEnumUnit = concentrationEnumUnit;
    }

    public String getConcentrationDenomUnit() {

        return concentrationDenomUnit;
    }

    public void setConcentrationDenomUnit(String concentrationDenomUnit) {

        this.concentrationDenomUnit = concentrationDenomUnit;
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
