package org.odhsi.athena.dto;

import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 09.07.2015.
 */
public class BrowserVocabularyTableDTO {

    String shortName;
    String fullName;

    public BrowserVocabularyTableDTO(Vocabulary vocabulary){
        this.setShortName(vocabulary.getId());
        this.setFullName(vocabulary.getName());
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
