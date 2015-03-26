package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by GMalikov on 26.03.2015.
 */
@Entity
@Table(name = "DRUG_STRENGTH")
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

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGREDIENT_CONCEPT_ID", nullable = false)
    public Concept getIngredientConcept() {
        return ingredientConcept;
    }

    public void setIngredientConcept(Concept ingredientConcept) {
        this.ingredientConcept = ingredientConcept;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DRUG_CONCEPT_ID", nullable = false)
    public Concept getDrugConcept() {
        return drugConcept;
    }


    public void setDrugConcept(Concept drugConcept) {
        this.drugConcept = drugConcept;
    }

    @Column(name = "AMOUNT_VALUE", nullable = true)
    public Float getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(Float amountValue) {
        this.amountValue = amountValue;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AMOUNT_UNIT_CONCEPT_ID", nullable = true)
    public Concept getAmountUnitConcept() {
        return amountUnitConcept;
    }

    public void setAmountUnitConcept(Concept amountUnitConcept) {
        this.amountUnitConcept = amountUnitConcept;
    }

    @Column(name = "NUMERATOR_VALUE", nullable = true)
    public Float getNumeratorValue() {
        return numeratorValue;
    }

    public void setNumeratorValue(Float numeratorValue) {
        this.numeratorValue = numeratorValue;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NUMERATOR_UNIT_CONCEPT_ID", nullable = true)
    public Concept getNumeratorUnitConcept() {
        return numeratorUnitConcept;
    }

    public void setNumeratorUnitConcept(Concept numeratorUnitConcept) {
        this.numeratorUnitConcept = numeratorUnitConcept;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DENOMINATOR_UNIT_CONCEPT_ID", nullable = true)
    public Concept getDenominatorUnitConcept() {
        return denominatorUnitConcept;
    }

    public void setDenominatorUnitConcept(Concept denominatorUnitConcept) {
        this.denominatorUnitConcept = denominatorUnitConcept;
    }

    @Column(name = "VALID_START_DATE", nullable = false)
    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
        this.validStartDate = validStartDate;
    }

    @Column(name = "VALID_END_DATE", nullable = false)
    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
        this.validEndDate = validEndDate;
    }

    @Column(name = "INVALID_REASON", nullable = true, length = 1)
    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
