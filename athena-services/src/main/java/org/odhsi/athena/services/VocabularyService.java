package org.odhsi.athena.services;

import org.odhsi.athena.dto.SimpleStatusDTO;
import org.odhsi.athena.dto.VocabularyStatusDTO;
import org.odhsi.athena.entity.Vocabulary;

import java.util.List;

/**
 * Created by GMalikov on 14.05.2015.
 */
public interface VocabularyService {

    public Vocabulary getById(String id);

    public List<Vocabulary> getAllVocabularies();

    public List<VocabularyStatusDTO> getAllVocabularyStatuses();

    public List<SimpleStatusDTO> getSimpleStatuses();

}
