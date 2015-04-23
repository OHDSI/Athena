package org.odhsi.athena.entity;

import java.util.Date;
import java.util.Set;

/**
 * Created by GMalikov on 25.03.2015.
 */

public class Concept {

    private Long Id;
    private String name;
    private Domain domain;
    private Vocabulary vocabulary;
    private ConceptClass conceptClass;
    private Boolean standard;
    private String code;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;
    private Set<ConceptAncestor> ancestors;
    private Set<ConceptAncestor> descendants;
    private Set<ConceptSynonym> synonyms;


    public Long getId() {
        return Id;
    }

    public void setId(Long concept_id) {
        this.Id = concept_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String concept_name) {
        this.name = concept_name;
    }

    public Boolean getStandard() {
        return standard;
    }

    public void setStandard(Boolean standard_concept) {
        this.standard = standard_concept;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String concept_code) {
        this.code = concept_code;
    }

    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStart) {
        this.validStartDate = validStart;
    }

    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEnd) {
        this.validEndDate = validEnd;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public ConceptClass getConceptClass() {
        return conceptClass;
    }

    public void setConceptClass(ConceptClass conceptClass) {
        this.conceptClass = conceptClass;
    }

    public Set<ConceptAncestor> getAncestors() {
        return ancestors;
    }

    public void setAncestors(Set<ConceptAncestor> ancestors) {
        this.ancestors = ancestors;
    }

    public Set<ConceptAncestor> getDescendants() {
        return descendants;
    }

    public void setDescendants(Set<ConceptAncestor> descendants) {
        this.descendants = descendants;
    }

    public Set<ConceptSynonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<ConceptSynonym> synonyms) {
        this.synonyms = synonyms;
    }
}
