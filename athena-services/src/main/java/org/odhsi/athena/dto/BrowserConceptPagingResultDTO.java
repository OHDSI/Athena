package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 20.07.2015.
 */
public class BrowserConceptPagingResultDTO {

    private int page;
    private long totalPages;
    private long records;
    private List<BrowserConceptTableDTO> data;

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

    public void setRecords(int records) {
        this.records = records;
    }

    public List<BrowserConceptTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserConceptTableDTO> data) {
        this.data = data;
    }
}
