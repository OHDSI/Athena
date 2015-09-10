package org.odhsi.athena.dto;

import java.util.List;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class BrowserRelationWithConceptPagingResultDTO extends BasePagingResultDTO{

    private List<BrowserRelationWithConceptTableDTO> data;

    public List<BrowserRelationWithConceptTableDTO> getData() {
        return data;
    }

    public void setData(List<BrowserRelationWithConceptTableDTO> data) {
        this.data = data;
    }
}
