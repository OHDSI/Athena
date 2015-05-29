package org.odhsi.athena.entity;

import java.util.Date;

/**
 * Created by GMalikov on 28.05.2015.
 */
public class VocabularyBuildLog {

    private Long id;
    private Date opStart;
    private Vocabulary vocabulary;
    private Long opNumber;
    private String opDescription;
    private Date opEnd;
    private Long opStatus;
    private String opDetail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getOpStart() {
        return opStart;
    }

    public void setOpStart(Date opStart) {
        this.opStart = opStart;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public Long getOpNumber() {
        return opNumber;
    }

    public void setOpNumber(Long opNumber) {
        this.opNumber = opNumber;
    }

    public String getOpDescription() {
        return opDescription;
    }

    public void setOpDescription(String opDescription) {
        this.opDescription = opDescription;
    }

    public Date getOpEnd() {
        return opEnd;
    }

    public void setOpEnd(Date opEnd) {
        this.opEnd = opEnd;
    }

    public Long getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(Long opStatus) {
        this.opStatus = opStatus;
    }

    public String getOpDetail() {
        return opDetail;
    }

    public void setOpDetail(String opDetail) {
        this.opDetail = opDetail;
    }
}
