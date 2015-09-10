package org.odhsi.athena.services;

import org.odhsi.athena.dto.BrowserVocabularyPagingResultDTO;
import org.odhsi.athena.dto.VocabularyBuildLogDTO;
import org.odhsi.athena.dto.VocabularyInfoDTO;
import org.odhsi.athena.dto.VocabularyStatusDTO;
import org.odhsi.athena.entity.Vocabulary;
import org.odhsi.athena.exceptions.VocabularyProcessingException;

import java.util.List;

/**
 * Created by GMalikov on 14.05.2015.
 */
public interface VocabularyService {

    public Vocabulary getById(String id);

    public List<Vocabulary> getAllVocabularies();

    public void buildVocabulary(String id) throws VocabularyProcessingException;

    public List<VocabularyStatusDTO> getAllVocabularyStatuses(String filter);

    public List<VocabularyBuildLogDTO> getLogForVocabulary(String vocabularyId, String filter);

    public VocabularyInfoDTO getInfoForVocabulary(String vocabularyId);

    public BrowserVocabularyPagingResultDTO getVocabulariesForBrowserTable(int start, int length, int page, String sortOrder, String searchVal);

}
