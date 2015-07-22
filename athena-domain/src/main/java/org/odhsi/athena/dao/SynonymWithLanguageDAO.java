package org.odhsi.athena.dao;

import org.odhsi.athena.entity.SynonymWithLanguage;

import java.util.List;

/**
 * Created by GMalikov on 22.07.2015.
 */
public interface SynonymWithLanguageDAO {

    public List<SynonymWithLanguage> getConceptSynonymsForBrowser(int start, int length, String searchValue, String sortOrder, Long conceptId);

    public int getTotalSynonyms();

    public int getFilteredSynonymsForBrowser(String searchValue, Long conceptId);
}
