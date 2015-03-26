package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by GMalikov on 26.03.2015.
 */
@Entity
@Table(name = "CONCEPT_CLASS")
public class ConceptClass {
    private String id;
    private String name;
    private Concept concept;
    private Set<Concept> conceptSet;

    @Id
    @Column(name = "CONCEPT_CLASS_ID", length = 20)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "CONCEPT_CLASS_NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_CLASS_CONCEPT_ID", nullable = false)
    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "conceptClass")
    public Set<Concept> getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(Set<Concept> conceptSet) {
        this.conceptSet = conceptSet;
    }
}
