package org.odhsi.athena.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by GMalikov on 27.03.2015.
 */
@Entity
@Table(name = "SOURCE_TO_CONCEPT_MAP")
public class SourceToConceptMap {

    private String sourceCode;
    private Concept sourceConcept;
    private Vocabulary sourceVocabulary;
    private String sourceCodeDescription;
    private Concept targetConcept;
    private Vocabulary targetVocabulary;
    private Date validStartDate;
    private Date validEndDate;
    private String invalidReason;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_CODE", nullable = false)
    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_CONCEPT_ID", nullable = false)
    public Concept getSourceConcept() {
        return sourceConcept;
    }

    public void setSourceConcept(Concept sourceConcept) {
        this.sourceConcept = sourceConcept;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_VOCABULARY_ID", nullable = false)
    public Vocabulary getSourceVocabulary() {
        return sourceVocabulary;
    }

    public void setSourceVocabulary(Vocabulary sourceVocabulary) {
        this.sourceVocabulary = sourceVocabulary;
    }

    @Column(name = "SOURCE_CODE_DESCRIPTION")
    public String getSourceCodeDescription() {
        return sourceCodeDescription;
    }

    public void setSourceCodeDescription(String sourceCodeDescription) {
        this.sourceCodeDescription = sourceCodeDescription;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_CONCEPT_ID", nullable = false)
    public Concept getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(Concept targetConcept) {
        this.targetConcept = targetConcept;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_VOCABULARY_ID", nullable = false)
    public Vocabulary getTargetVocabulary() {
        return targetVocabulary;
    }

    public void setTargetVocabulary(Vocabulary targetVocabulary) {
        this.targetVocabulary = targetVocabulary;
    }

    @Column(name = "VALID_START_DATE", nullable = false)
    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
        this.validStartDate = validStartDate;
    }

    @Id
    @Column(name = "VALID_END_DATE", nullable = false)
    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
        this.validEndDate = validEndDate;
    }

    @Column(name = "INVALID_REASON", nullable = false, length = 1)
    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
