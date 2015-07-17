package org.odhsi.athena.dto;

import org.odhsi.athena.entity.DomainWithConceptCount;

/**
 * Created by GMalikov on 16.07.2015.
 */
public class BrowserDomainWithConceptCountTableDTO {
    String id;
    long conceptCount;

    public BrowserDomainWithConceptCountTableDTO(DomainWithConceptCount domainWithConceptCount){
        this.setId(domainWithConceptCount.getId());
        this.setConceptCount(domainWithConceptCount.getConceptCount());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getConceptCount() {
        return conceptCount;
    }

    public void setConceptCount(long conceptCount) {
        this.conceptCount = conceptCount;
    }
}
