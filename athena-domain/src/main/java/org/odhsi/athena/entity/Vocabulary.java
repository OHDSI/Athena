package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by GMalikov on 26.03.2015.
 */

@Entity
@Table(name = "VOCABULARY")
public class Vocabulary {

    private String id;
    private String name;
    private String reference;
    private String version;
    private Concept concept;
    private Set<Concept> conceptSet;


    @Id
    @Column(name = "VOCABULARY_ID", length = 20)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "VOCABULARY_NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "VOCABULARY_REFERENCE", nullable = true)
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Column(name = "VOCABULARY_VERSION", nullable = true)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOCABULARY_CONCEPT_ID", nullable = false)
    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vocabulary")
    public Set<Concept> getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(Set<Concept> conceptSet) {
        this.conceptSet = conceptSet;
    }
}
