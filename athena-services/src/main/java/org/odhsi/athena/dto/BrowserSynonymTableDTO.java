package org.odhsi.athena.dto;

import org.odhsi.athena.entity.SynonymWithLanguage;

/**
 * Created by GMalikov on 22.07.2015.
 */
public class BrowserSynonymTableDTO {

    private String name;
    private String language;

    public BrowserSynonymTableDTO(SynonymWithLanguage synonymWithLanguage){
        this.setName(synonymWithLanguage.getName());
        this.setLanguage(synonymWithLanguage.getLanguage());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
