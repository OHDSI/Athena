package org.odhsi.athena.dto;

import org.odhsi.athena.entity.RelationWithConcept;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class BrowserRelationWithConceptTableDTO {
    private String relationName;
    private String conceptName;

    public BrowserRelationWithConceptTableDTO(RelationWithConcept relationWithConcept){
        this.setRelationName(relationWithConcept.getId());
        this.setConceptName(relationWithConcept.getConceptName());
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }
}
