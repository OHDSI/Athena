package org.odhsi.athena.entity;

import javax.persistence.*;

/**
 * Created by GMalikov on 27.03.2015.
 */
@Entity
@Table(name = "RELATIONSHIP")
public class Relationship {

    private String id;
    private String name;
    private String hierarchical;
    private String definesAncestry;
    private Relationship reverse;
    private Concept concept;

    @Id
    @Column(name = "RELATIONSHIP_ID", length = 20)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "RELATIONSHIP_NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "IS_HIERARCHICAL", nullable = false, length = 1)
    public String getHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(String hierarchical) {
        this.hierarchical = hierarchical;
    }

    @Column(name = "DEFINES_ANCESTRY", nullable = false, length = 1)
    public String getDefinesAncestry() {
        return definesAncestry;
    }

    public void setDefinesAncestry(String definesAncestry) {
        this.definesAncestry = definesAncestry;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REVERSE_RELATIONSHIP_ID", nullable = false)
    public Relationship getReverse() {
        return reverse;
    }

    public void setReverse(Relationship reverse) {
        this.reverse = reverse;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATIONSHIP_CONCEPT_ID", nullable = false)
    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
