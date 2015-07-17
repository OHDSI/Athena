package org.odhsi.athena.services;

import org.odhsi.athena.dto.BrowserDomainWithConceptCountTableDTO;

import java.util.List;

/**
 * Created by GMalikov on 16.07.2015.
 */
public interface DomainService {

    public List<BrowserDomainWithConceptCountTableDTO> getDomainsForBrowserByVocabularyId(String vocabularyId);
}
