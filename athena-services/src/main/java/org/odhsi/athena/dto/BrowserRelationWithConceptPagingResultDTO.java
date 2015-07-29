package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class BrowserRelationWithConceptPagingResultDTO {
    private int page;
    private long totalPages;
    private long records;
    private List<BrowserRelationWithConceptTableDTO> data;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public long getRecords() {
        return records;
    }

    public void setRecords(long records) {
        this.records = records;
    }

    public List<BrowserRelationWithConceptTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserRelationWithConceptTableDTO> data) {
        this.data = data;
    }
}
