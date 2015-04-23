package org.odhsi.athena.entity;

/**
 * Created by GMalikov on 30.03.2015.
 */
public class AttributeDefinition {

    private Long id;
    private String name;
    private Character[] description;
    private Concept typeConcept;
    private Character[] syntax;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character[] getDescription() {
        return description;
    }

    public void setDescription(Character[] description) {
        this.description = description;
    }

    public Concept getTypeConcept() {
        return typeConcept;
    }

    public void setTypeConcept(Concept typeConcept) {
        this.typeConcept = typeConcept;
    }

    public Character[] getSyntax() {
        return syntax;
    }

    public void setSyntax(Character[] syntax) {
        this.syntax = syntax;
    }
}
