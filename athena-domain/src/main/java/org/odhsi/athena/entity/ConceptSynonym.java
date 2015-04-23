package org.odhsi.athena.entity;


/**
 * Created by GMalikov on 27.03.2015.
 */

public class ConceptSynonym {

    private Concept concept;
    private Concept languageConcept;
    private String synonymName;

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public Concept getLanguageConcept() {
        return languageConcept;
    }

    public void setLanguageConcept(Concept languageConcept) {
        this.languageConcept = languageConcept;
    }

    public String getSynonymName() {
        return synonymName;
    }

    public void setSynonymName(String synonymName) {
        this.synonymName = synonymName;
    }
}
