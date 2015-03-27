package org.odhsi.athena.entity;

import javax.persistence.*;

/**
 * Created by GMalikov on 27.03.2015.
 */
@Entity
@Table(name = "CONCEPT_SYNONYM")
public class ConceptSynonym {

    private Concept concept;
    private Concept languageConcept;
    private String synonymName;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_ID")
    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANGUAGE_CONCEPT_ID")
    public Concept getLanguageConcept() {
        return languageConcept;
    }

    public void setLanguageConcept(Concept languageConcept) {
        this.languageConcept = languageConcept;
    }

    @Column(name = "CONCEPT_SYNONYM_NAME")
    public String getSynonymName() {
        return synonymName;
    }

    public void setSynonymName(String synonymName) {
        this.synonymName = synonymName;
    }
}
