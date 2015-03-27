package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by GMalikov on 27.03.2015.
 */
@Entity
@Table(name = "CONCEPT_RELATIONSHIP")
public class ConceptRelationship {

    private Concept first;
    private Concept second;
    private Relationship relation;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_ID_1", nullable = false)
    public Concept getFirst() {
        return first;
    }

    public void setFirst(Concept first) {
        this.first = first;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_ID_2", nullable = false)
    public Concept getSecond() {
        return second;
    }

    public void setSecond(Concept second) {
        this.second = second;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATIONSHIP_ID", nullable = false)
    public Relationship getRelation() {
        return relation;
    }

    public void setRelation(Relationship relation) {
        this.relation = relation;
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

    @Column(name = "INVALID_REASON", nullable = true)
    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
