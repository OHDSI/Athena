package org.odhsi.athena.dto;

import org.odhsi.athena.entity.DomainWithConceptCount;

/**
 * Created by GMalikov on 16.07.2015.
 */
public class BrowserDomainWithConceptCountTableDTO {
    private String domain;
    private long conceptCount;

    public BrowserDomainWithConceptCountTableDTO(DomainWithConceptCount domainWithConceptCount){
        this.setDomain(domainWithConceptCount.getId());
        this.setConceptCount(domainWithConceptCount.getConceptCount());
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getConceptCount() {
        return conceptCount;
    }

    public void setConceptCount(long conceptCount) {
        this.conceptCount = conceptCount;
    }
}
