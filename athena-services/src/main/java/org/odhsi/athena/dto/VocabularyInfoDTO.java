package org.odhsi.athena.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Created by GMalikov on 30.06.2015.
 */
public class VocabularyInfoDTO {

    private String id;
    private String name;
    private long recordsCount;
    private long domainsCount;
    private long conceptsCount;
    private long relationsCount;
    private String sourceName;
    private String intersections;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date lastUpdated;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(long recordsCount) {
        this.recordsCount = recordsCount;
    }

    public long getDomainsCount() {
        return domainsCount;
    }

    public void setDomainsCount(long domainsCount) {
        this.domainsCount = domainsCount;
    }

    public long getConceptsCount() {
        return conceptsCount;
    }

    public void setConceptsCount(long conceptsCount) {
        this.conceptsCount = conceptsCount;
    }

    public long getRelationsCount() {
        return relationsCount;
    }

    public void setRelationsCount(long relationsCount) {
        this.relationsCount = relationsCount;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getIntersections() {
        return intersections;
    }

    public void setIntersections(String intersections) {
        this.intersections = intersections;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
