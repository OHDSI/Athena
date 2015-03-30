package org.odhsi.athena.entity;

import javax.persistence.*;

/**
 * Created by GMalikov on 30.03.2015.
 */
@Entity
@Table(name = "ATTRIBUTE_DEFINITION")
public class AttributeDefinition {

    private Long id;
    private String name;
    private Character[] description;
    private Concept typeConcept;
    private Character[] syntax;

    @Id
    @Column(name = "ATTRIBUTE_DEFINITION_ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ATTRIBUTE_NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Lob
    @Column(name = "ATTRIBUTE_DESCRIPTION", nullable = true)
    public Character[] getDescription() {
        return description;
    }

    public void setDescription(Character[] description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATTRIBUTE_TYPE_CONCEPT_ID", nullable = false)
    public Concept getTypeConcept() {
        return typeConcept;
    }

    public void setTypeConcept(Concept typeConcept) {
        this.typeConcept = typeConcept;
    }

    @Lob
    @Column(name = "ATTRIBUTE_SYNTAX", nullable = true)
    public Character[] getSyntax() {
        return syntax;
    }

    public void setSyntax(Character[] syntax) {
        this.syntax = syntax;
    }
}
