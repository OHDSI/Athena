package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Concept;

/**
 * Created by GMalikov on 20.07.2015.
 */
public class BrowserConceptTableDTO {
    private Long id;
    private String name;

    public BrowserConceptTableDTO(Concept concept){
        this.setId(concept.getId());
        this.setName(concept.getName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
