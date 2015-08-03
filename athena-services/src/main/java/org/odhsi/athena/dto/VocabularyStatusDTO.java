package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 14.05.2015.
 */
public class VocabularyStatusDTO {
    public static final String BUILD_IN_PROGRESS = "Build in progress";
    public static final String READY = "Ready";
    public static final String READY_WITH_NOTICES = "Ready, but have notices";
    public static final String FAILED = "Failed";
    public static final String NOT_AVAILABLE = "Not available";

    private String id;
    private String name;
    private String status = "4";
    private String statusName = "Unavailable";
    private String opNumber;
    private String description;
    private String detail;


    public VocabularyStatusDTO(Vocabulary vocabulary){
        this.id = vocabulary.getId();
        this.name = vocabulary.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if(status != null){
            this.status = status;
        }
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getOpNumber() {
        return opNumber;
    }

    public void setOpNumber(String opNumber) {
        this.opNumber = opNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
