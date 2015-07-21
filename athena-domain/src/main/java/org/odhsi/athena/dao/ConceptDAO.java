package org.odhsi.athena.dao;

import org.odhsi.athena.entity.Concept;

import java.util.List;

/**
 * Created by GMalikov on 25.03.2015.
 */
public interface ConceptDAO {

    public List<Concept> getPagingConceptsForBrowser(int start, int length, String searchValue, String sortOrder, String vocabularyId, String domainId);

    public int getTotalConceptsForBrowser();

    public int getFilteredConceptsCountForBrowser(String vocabularyId, String domainId, String searchValue);
}
