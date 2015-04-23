package org.odhsi.athena.entity;

import java.util.Date;

/**
 * Created by GMalikov on 26.03.2015.
 */
public class DrugStrength {

    private Concept drugConcept;
    private Concept ingredientConcept;
    private Float amountValue;
    private Concept amountUnitConcept;
    private Float numeratorValue;
    private Concept numeratorUnitConcept;
    private Concept denominatorUnitConcept;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;

    public Concept getIngredientConcept() {
        return ingredientConcept;
    }

    public void setIngredientConcept(Concept ingredientConcept) {
        this.ingredientConcept = ingredientConcept;
    }

    public Concept getDrugConcept() {
        return drugConcept;
    }

    public void setDrugConcept(Concept drugConcept) {
        this.drugConcept = drugConcept;
    }

    public Float getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(Float amountValue) {
        this.amountValue = amountValue;
    }

    public Concept getAmountUnitConcept() {
        return amountUnitConcept;
    }

    public void setAmountUnitConcept(Concept amountUnitConcept) {
        this.amountUnitConcept = amountUnitConcept;
    }

    public Float getNumeratorValue() {
        return numeratorValue;
    }

    public void setNumeratorValue(Float numeratorValue) {
        this.numeratorValue = numeratorValue;
    }

    public Concept getNumeratorUnitConcept() {
        return numeratorUnitConcept;
    }

    public void setNumeratorUnitConcept(Concept numeratorUnitConcept) {
        this.numeratorUnitConcept = numeratorUnitConcept;
    }

    public Concept getDenominatorUnitConcept() {
        return denominatorUnitConcept;
    }

    public void setDenominatorUnitConcept(Concept denominatorUnitConcept) {
        this.denominatorUnitConcept = denominatorUnitConcept;
    }

    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
        this.validStartDate = validStartDate;
    }

    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
        this.validEndDate = validEndDate;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
