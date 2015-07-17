package org.odhsi.athena.entity;

/**
 * Created by GMalikov on 17.07.2015.
 */
public class DomainWithConceptCount {

    private String id;
    private long conceptCount;

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
