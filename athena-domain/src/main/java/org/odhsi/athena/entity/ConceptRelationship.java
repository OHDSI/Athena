package org.odhsi.athena.entity;

import java.util.Date;

/**
 * Created by GMalikov on 27.03.2015.
 */
public class ConceptRelationship {

    private Concept first;
    private Concept second;
    private Relationship relation;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;

    public Concept getFirst() {
        return first;
    }

    public void setFirst(Concept first) {
        this.first = first;
    }

    public Concept getSecond() {
        return second;
    }

    public void setSecond(Concept second) {
        this.second = second;
    }

    public Relationship getRelation() {
        return relation;
    }

    public void setRelation(Relationship relation) {
        this.relation = relation;
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
