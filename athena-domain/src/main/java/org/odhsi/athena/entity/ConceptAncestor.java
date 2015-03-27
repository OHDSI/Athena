package org.odhsi.athena.entity;

import javax.persistence.*;

/**
 * Created by GMalikov on 27.03.2015.
 */
@Entity
@Table(name = "CONCEPT_ANCESTOR")
public class ConceptAncestor {

    private Concept ancestorConcept;
    private Concept descendantConcept;
    private long minLevelsOfSeparation;
    private long maxLevelsOfSeparation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ANCESTOR_CONCEPT_ID", nullable = false)
    public Concept getAncestorConcept() {
        return ancestorConcept;
    }

    public void setAncestorConcept(Concept ancestorConcept) {
        this.ancestorConcept = ancestorConcept;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESCENDANT_CONCEPT_ID", nullable = false)
    public Concept getDescendantConcept() {
        return descendantConcept;
    }

    public void setDescendantConcept(Concept descendantConcept) {
        this.descendantConcept = descendantConcept;
    }

    @Column(name = "MIN_LEVELS_OF_SEPARATION", nullable = false)
    public long getMinLevelsOfSeparation() {
        return minLevelsOfSeparation;
    }

    public void setMinLevelsOfSeparation(long minLevelsOfSeparation) {
        this.minLevelsOfSeparation = minLevelsOfSeparation;
    }

    @Column(name = "MAX_LEVELS_OF_SEPARATION", nullable = false)
    public long getMaxLevelsOfSeparation() {
        return maxLevelsOfSeparation;
    }

    public void setMaxLevelsOfSeparation(long maxLevelsOfSeparation) {
        this.maxLevelsOfSeparation = maxLevelsOfSeparation;
    }
}
