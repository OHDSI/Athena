package org.odhsi.athena.entity;

import java.util.Set;

/**
 * Created by GMalikov on 26.03.2015.
 */
public class ConceptClass {
    private String id;
    private String name;
    private Concept concept;
    private Set<Concept> conceptSet;

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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public Set<Concept> getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(Set<Concept> conceptSet) {
        this.conceptSet = conceptSet;
    }
}
