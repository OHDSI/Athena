package org.odhsi.athena.dao;

import org.odhsi.athena.entity.Vocabulary;

import java.util.Date;
import java.util.List;

/**
 * Created by GMalikov on 26.03.2015.
 */

public interface VocabularyDAO{

    public Vocabulary getVocabularyById(String id);

    public List<Vocabulary> getAllVocabularies();

    public String getVocabularyStatus(String id);

    public Date getLatestUpdateFromConversion(String id);

    public void buildVocabulary(String id, String version, Date latestUpdate);

    public long getRecordsTotalForVocabulary(String vocabularyId);

    public long getDomainsCountForVocabulary(String vocabularyId);

    public long getConceptsCountForVocabulary(String vocabularyId);

    public long getRelationsCountForVocabulary(String vocabularyId);
}
