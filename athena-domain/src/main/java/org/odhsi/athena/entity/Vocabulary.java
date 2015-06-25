package org.odhsi.athena.entity;

import java.util.Date;
import java.util.Set;

/**
 * Created by GMalikov on 26.03.2015.
 */

public class Vocabulary {

    private String id;
    private String name;
    private String reference;
    private String version;
    private Concept concept;
    private Set<Concept> conceptSet;
    private Date latestUpdate;


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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Date getLatestUpdate() {
        return latestUpdate;
    }

    public void setLatestUpdate(Date latestUpdate) {
        this.latestUpdate = latestUpdate;
    }
}
