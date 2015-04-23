package org.odhsi.athena.entity;


/**
 * Created by GMalikov on 27.03.2015.
 */
public class ConceptAncestor {

    private Concept ancestorConcept;
    private Concept descendantConcept;
    private long minLevelsOfSeparation;
    private long maxLevelsOfSeparation;

    public Concept getAncestorConcept() {
        return ancestorConcept;
    }

    public void setAncestorConcept(Concept ancestorConcept) {
        this.ancestorConcept = ancestorConcept;
    }

    public Concept getDescendantConcept() {
        return descendantConcept;
    }

    public void setDescendantConcept(Concept descendantConcept) {
        this.descendantConcept = descendantConcept;
    }

    public long getMinLevelsOfSeparation() {
        return minLevelsOfSeparation;
    }

    public void setMinLevelsOfSeparation(long minLevelsOfSeparation) {
        this.minLevelsOfSeparation = minLevelsOfSeparation;
    }

    public long getMaxLevelsOfSeparation() {
        return maxLevelsOfSeparation;
    }

    public void setMaxLevelsOfSeparation(long maxLevelsOfSeparation) {
        this.maxLevelsOfSeparation = maxLevelsOfSeparation;
    }
}
