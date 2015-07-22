package org.odhsi.athena.entity;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class RelationWithConcept {

    private String id;
    private String conceptName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }
}
