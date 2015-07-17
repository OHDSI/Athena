package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 10.07.2015.
 */
public class BrowserVocabularyPagingResultDTO {
    int draw;
    int recordsTotal;
    int recordsFiltered;
    List<BrowserVocabularyTableDTO> data;

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public List<BrowserVocabularyTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserVocabularyTableDTO> data) {
        this.data = data;
    }
}
