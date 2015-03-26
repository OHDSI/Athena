package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by GMalikov on 25.03.2015.
 */

@Entity
@Table(name = "DOMAIN")
public class Domain {

    private String id;
    private String name;
    private Set<Concept> conceptSet;
    private Concept concept;


    @Id
    @Column(name = "DOMAIN_ID", nullable = false, length = 20)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "DOMAIN_NAME", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "domain")
    public Set<Concept> getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(Set<Concept> conceptSet) {
        this.conceptSet = conceptSet;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_CONCEPT_ID", nullable = false)
    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
