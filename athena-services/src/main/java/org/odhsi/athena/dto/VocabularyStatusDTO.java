package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 14.05.2015.
 */
public class VocabularyStatusDTO {
    private String id;
    private String name;
    private Long status;
    private String statusName;

    public VocabularyStatusDTO(Vocabulary vocabulary){
        this.id = vocabulary.getId();
        this.name = vocabulary.getName();
        this.status = 0L;
        this.statusName = "Ready";
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

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
