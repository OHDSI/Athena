package org.odhsi.athena.entity;


import java.util.Date;

/**
 * Created by GMalikov on 30.03.2015.
 */

public class CohortDefinition {

    private Long id;
    private String name;
    private Character[] description;
    private Concept definitionType;
    private Character[] syntax;
    private Concept subject;
    private Date initiationDate;

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

    public Concept getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(Concept definitionType) {
        this.definitionType = definitionType;
    }

    public Character[] getSyntax() {
        return syntax;
    }

    public void setSyntax(Character[] syntax) {
        this.syntax = syntax;
    }

    public Concept getSubject() {
        return subject;
    }

    public void setSubject(Concept subject) {
        this.subject = subject;
    }

    public Date getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(Date initiationDate) {
        this.initiationDate = initiationDate;
    }
}
