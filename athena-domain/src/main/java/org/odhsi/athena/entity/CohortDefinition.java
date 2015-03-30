package org.odhsi.athena.entity;


import javax.persistence.*;
import java.util.Date;

/**
 * Created by GMalikov on 30.03.2015.
 */
@Entity
@Table(name = "COHORT_DEFINITION")
public class CohortDefinition {

    private Long id;
    private String name;
    private Character[] description;
    private Concept definitionType;
    private Character[] syntax;
    private Concept subject;
    private Date initiationDate;

    @Id
    @Column(name = "COHORT_DEFINITION_ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "COHORT_DEFINITION_NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Lob
    @Column(name = "COHORT_DEFINITION_DESCRIPTION", nullable = true)
    public Character[] getDescription() {
        return description;
    }

    public void setDescription(Character[] description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_TYPE_CONCEPT_ID", nullable = false)
    public Concept getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(Concept definitionType) {
        this.definitionType = definitionType;
    }

    @Lob
    @Column(name = "COHORT_DEFINITION_SYNTAX", nullable = true)
    public Character[] getSyntax() {
        return syntax;
    }

    public void setSyntax(Character[] syntax) {
        this.syntax = syntax;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBJECT_CONCEPT_ID", nullable = false)
    public Concept getSubject() {
        return subject;
    }

    public void setSubject(Concept subject) {
        this.subject = subject;
    }

    @Column(name = "COHORT_INITIATION_DATE", nullable = false)
    public Date getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(Date initiationDate) {
        this.initiationDate = initiationDate;
    }
}
