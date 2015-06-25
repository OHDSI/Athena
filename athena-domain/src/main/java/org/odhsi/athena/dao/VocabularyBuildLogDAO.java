package org.odhsi.athena.dao;

import org.odhsi.athena.entity.VocabularyBuildLog;

import java.util.List;

/**
 * Created by GMalikov on 28.05.2015.
 */
public interface VocabularyBuildLogDAO {

    public List<VocabularyBuildLog> getLogForVocabulary(String vocabularyId);

    public String getVocabularyStatus(String vocabularyId);
}
