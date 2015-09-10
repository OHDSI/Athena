package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 20.07.2015.
 */
public class BrowserConceptPagingResultDTO extends BasePagingResultDTO{

    private List<BrowserConceptTableDTO> data;

    public List<BrowserConceptTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserConceptTableDTO> data) {
        this.data = data;
    }
}
