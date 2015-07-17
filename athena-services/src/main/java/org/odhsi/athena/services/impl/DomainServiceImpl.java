package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.DomainWithConceptCountDAO;
import org.odhsi.athena.dto.BrowserDomainWithConceptCountTableDTO;
import org.odhsi.athena.entity.DomainWithConceptCount;
import org.odhsi.athena.services.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 16.07.2015.
 */
public class DomainServiceImpl implements DomainService {

    @Autowired
    private DomainWithConceptCountDAO domainWithConceptCountDAO;

    @Override
    public List<BrowserDomainWithConceptCountTableDTO> getDomainsForBrowserByVocabularyId(String vocabularyId) {
        List<BrowserDomainWithConceptCountTableDTO> result = new ArrayList<>();
        if (vocabularyId != null){
            for (DomainWithConceptCount domainWithConceptCount : domainWithConceptCountDAO.getDomainsWithConceptCountForVocabulary(vocabularyId)){
                result.add(new BrowserDomainWithConceptCountTableDTO(domainWithConceptCount));
            }
        }
        return result;
    }
}
