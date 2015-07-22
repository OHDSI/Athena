package org.odhsi.athena.services;

import org.odhsi.athena.dto.BrowserSynonymPagingResultDTO;

/**
 * Created by GMalikov on 22.07.2015.
 */
public interface SynonymService {

    public BrowserSynonymPagingResultDTO getPagingSynonymsForBrowser(int draw, int start, int length, String searchValue, String sortOrder, Long conceptId);
}
