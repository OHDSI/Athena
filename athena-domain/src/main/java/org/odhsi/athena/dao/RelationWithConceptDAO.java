package org.odhsi.athena.dao;

import org.odhsi.athena.entity.RelationWithConcept;

import java.util.List;

/**
 * Created by GMalikov on 21.07.2015.
 */
public interface RelationWithConceptDAO {
    public List<RelationWithConcept> getRelationsForBrowserPagingResult(int start, int length, String sortOrder, String searchValue, Long conceptId);

    public int getTotalRelationsWithConcepts();

    public int getFilteredRelationsForBrowser(String searchValue, Long conceptId);
}
