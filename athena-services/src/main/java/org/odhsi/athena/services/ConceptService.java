package org.odhsi.athena.services;

import org.odhsi.athena.dto.BrowserConceptPagingResultDTO;

/**
 * Created by GMalikov on 20.07.2015.
 */
public interface ConceptService {

    public BrowserConceptPagingResultDTO getPagingConceptsForBrowser(int draw, int start, int length, String searchValue, String sortOrder, String vocabularyId, String domainId);

}
