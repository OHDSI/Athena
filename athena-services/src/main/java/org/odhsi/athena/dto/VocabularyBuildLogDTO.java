package org.odhsi.athena.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.odhsi.athena.entity.VocabularyBuildLog;

import java.util.Date;

/**
 * Created by GMalikov on 29.05.2015.
 */
public class VocabularyBuildLogDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date opStart;
    private Long opNumber;
    private String opDescription;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date opEnd;
    private Long opStatus;
    private String opDetail;

    public VocabularyBuildLogDTO(VocabularyBuildLog vocabularyBuildLog){
        this.setId(vocabularyBuildLog.getId());
        this.setOpStart(vocabularyBuildLog.getOpStart());
        this.setOpNumber(vocabularyBuildLog.getOpNumber());
        this.setOpDescription(vocabularyBuildLog.getOpDescription());
        this.setOpEnd(vocabularyBuildLog.getOpEnd());
        this.setOpStatus(vocabularyBuildLog.getOpStatus());
        this.setOpDetail(vocabularyBuildLog.getOpDetail());
    }

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
