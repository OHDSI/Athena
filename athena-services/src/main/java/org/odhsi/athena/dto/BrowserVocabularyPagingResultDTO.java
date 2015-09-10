package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 10.07.2015.
 */
public class BrowserVocabularyPagingResultDTO extends BasePagingResultDTO {

    private List<BrowserVocabularyTableDTO> data;

    public List<BrowserVocabularyTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserVocabularyTableDTO> data) {
        this.data = data;
    }
}
