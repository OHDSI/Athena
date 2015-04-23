package org.odhsi.athena.entity;

import java.util.Date;

/**
 * Created by GMalikov on 30.03.2015.
 */
public class CohortAttribute {

    private CohortDefinition cohortDefinition;
    private Date cohortStartDate;
    private Date cohortEndDate;
    private Concept subject;
    private AttributeDefinition attributeDefinition;
    private Float numberValue;
    private Concept conceptValue;

    public CohortDefinition getCohortDefinition() {
        return cohortDefinition;
    }

    public void setCohortDefinition(CohortDefinition cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    public Date getCohortStartDate() {
        return cohortStartDate;
    }

    public void setCohortStartDate(Date cohortStartDate) {
        this.cohortStartDate = cohortStartDate;
    }

    public Date getCohortEndDate() {
        return cohortEndDate;
    }

    public void setCohortEndDate(Date cohortEndDate) {
        this.cohortEndDate = cohortEndDate;
    }

    public Concept getSubject() {
        return subject;
    }

    public void setSubject(Concept subject) {
        this.subject = subject;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public Float getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Float numberValue) {
        this.numberValue = numberValue;
    }

    public Concept getConceptValue() {
        return conceptValue;
    }

    public void setConceptValue(Concept conceptValue) {
        this.conceptValue = conceptValue;
    }
}
