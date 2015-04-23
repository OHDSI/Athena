package org.odhsi.athena.entity;

import java.util.Set;

/**
 * Created by GMalikov on 25.03.2015.
 */


public class Domain {

    private String id;
    private String name;
    private Set<Concept> conceptSet;
    private Concept concept;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Concept> getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(Set<Concept> conceptSet) {
        this.conceptSet = conceptSet;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
