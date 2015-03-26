package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by GMalikov on 25.03.2015.
 */

@Entity
@Table(name = "CONCEPT")
public class Concept {

    private Long Id;
    private String name;
    private Domain domain;
    private Vocabulary vocabulary;
    //place for concept_class_id
    private Boolean standard;
    private String code;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;
    private Set<Domain> domainSet;
    private Set<Vocabulary> vocabularySet;


    @Id
    @Column(name = "CONCEPT_ID", nullable = false)
    public Long getId() {
        return Id;
    }

    public void setId(Long concept_id) {
        this.Id = concept_id;
    }

    @Column(name = "CONCEPT_NAME", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String concept_name) {
        this.name = concept_name;
    }

    @Column(name = "STANDARD_CONCEPT", nullable = true)
    public Boolean getStandard() {
        return standard;
    }

    public void setStandard(Boolean standard_concept) {
        this.standard = standard_concept;
    }

    @Column(name = "CONCEPT_CODE", nullable = false, length = 50)
    public String getCode() {
        return code;
    }

    public void setCode(String concept_code) {
        this.code = concept_code;
    }

    @Column(name = "VALID_START_DATE", nullable = false)
    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStart) {
        this.validStartDate = validStart;
    }

    @Column(name = "VALID_END_DATE", nullable = false)
    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEnd) {
        this.validEndDate = validEnd;
    }

    @Column(name = "INVALID_REASON", nullable = true)
    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_ID", nullable = false)
    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "concept")
    public Set<Domain> getDomainSet() {
        return domainSet;
    }

    public void setDomainSet(Set<Domain> domainSet) {
        this.domainSet = domainSet;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOCABULARY_ID", nullable = false)
    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "concept")
    public Set<Vocabulary> getVocabularySet() {
        return vocabularySet;
    }

    public void setVocabularySet(Set<Vocabulary> vocabularySet) {
        this.vocabularySet = vocabularySet;
    }


}
