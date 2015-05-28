package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 27.05.2015.
 */
public class SimpleStatusDTO {
    private String name;
    private String status;
    private String controls;

    public SimpleStatusDTO(Vocabulary vocabulary){
        this.name = vocabulary.getId();
        this.status = "Ready";
        this.controls = "";
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
        this.status = status;
    }

    public String getControls() {
        return controls;
    }

    public void setControls(String controls) {
        this.controls = controls;
    }
}
