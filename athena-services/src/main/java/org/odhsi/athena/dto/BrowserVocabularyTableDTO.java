package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 09.07.2015.
 */
public class BrowserVocabularyTableDTO {

    String id;
    String name;
    String fullName;

    public BrowserVocabularyTableDTO(Vocabulary vocabulary){
        this.setId(vocabulary.getId().replace(" ","_"));
        this.setName(vocabulary.getId());
        this.setFullName(vocabulary.getName());
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
