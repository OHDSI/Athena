package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by GMalikov on 30.03.2015.
 */
@Entity
@Table(name = "COHORT_ATTRIBUTE")
public class CohortAttribute {

    private CohortDefinition cohortDefinition;
    private Date cohortStartDate;
    private Date cohortEndDate;
    private Concept subject;
    private AttributeDefinition attributeDefinition;
    private Float numberValue;
    private Concept conceptValue;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COHORT_DEFINITION_ID", nullable = false)
    public CohortDefinition getCohortDefinition() {
        return cohortDefinition;
    }

    public void setCohortDefinition(CohortDefinition cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    @Id
    @Column(name = "COHORT_START_DATE", nullable = false)
    public Date getCohortStartDate() {
        return cohortStartDate;
    }

    public void setCohortStartDate(Date cohortStartDate) {
        this.cohortStartDate = cohortStartDate;
    }

    @Id
    @Column(name = "COHORT_END_DATE", nullable = false)
    public Date getCohortEndDate() {
        return cohortEndDate;
    }

    public void setCohortEndDate(Date cohortEndDate) {
        this.cohortEndDate = cohortEndDate;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBJECT_ID", nullable = false)
    public Concept getSubject() {
        return subject;
    }

    public void setSubject(Concept subject) {
        this.subject = subject;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATTRIBUTE_DEFINITION_ID", nullable = false)
    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    @Column(name = "VALUE_AS_NUMBER", nullable = true)
    public Float getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Float numberValue) {
        this.numberValue = numberValue;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VALUE_AS_CONCEPT_ID", nullable = true)
    public Concept getConceptValue() {
        return conceptValue;
    }

    public void setConceptValue(Concept conceptValue) {
        this.conceptValue = conceptValue;
    }
}
