package org.odhsi.athena.dao;

import org.odhsi.athena.entity.DomainWithConceptCount;

import java.util.List;

/**
 * Created by GMalikov on 17.07.2015.
 */
public interface DomainWithConceptCountDAO {

    public List<DomainWithConceptCount> getDomainsWithConceptCountForVocabulary(String vocabularyId);
}
