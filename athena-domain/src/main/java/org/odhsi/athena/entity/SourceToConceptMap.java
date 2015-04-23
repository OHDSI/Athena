package org.odhsi.athena.entity;

import java.util.Date;

/**
 * Created by GMalikov on 27.03.2015.
 */

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

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Concept getSourceConcept() {
        return sourceConcept;
    }

    public void setSourceConcept(Concept sourceConcept) {
        this.sourceConcept = sourceConcept;
    }

    public Vocabulary getSourceVocabulary() {
        return sourceVocabulary;
    }

    public void setSourceVocabulary(Vocabulary sourceVocabulary) {
        this.sourceVocabulary = sourceVocabulary;
    }

    public String getSourceCodeDescription() {
        return sourceCodeDescription;
    }

    public void setSourceCodeDescription(String sourceCodeDescription) {
        this.sourceCodeDescription = sourceCodeDescription;
    }

    public Concept getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(Concept targetConcept) {
        this.targetConcept = targetConcept;
    }

    public Vocabulary getTargetVocabulary() {
        return targetVocabulary;
    }

    public void setTargetVocabulary(Vocabulary targetVocabulary) {
        this.targetVocabulary = targetVocabulary;
    }

    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
        this.validStartDate = validStartDate;
    }

    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
        this.validEndDate = validEndDate;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
