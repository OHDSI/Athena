package org.odhsi.athena.dto;

/**
 * Created by GMalikov on 10.09.2015.
 */
public class BasePagingResultDTO {
    private int page;
    private long totalPages;
    private long records;

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
}
