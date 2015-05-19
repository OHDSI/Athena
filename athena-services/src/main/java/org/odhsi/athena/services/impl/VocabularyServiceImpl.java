package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.dto.VocabularyStatusDTO;
import org.odhsi.athena.entity.Vocabulary;
import org.odhsi.athena.services.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 14.05.2015.
 */
public class VocabularyServiceImpl implements VocabularyService{
    @Autowired
    private VocabularyDAO vocabularyDAO;

    @Override
    public Vocabulary getById(String id) {
        return vocabularyDAO.getVocabularyById(id);
    }

    @Override
    public List<Vocabulary> getAllVocabularies() {
        return vocabularyDAO.getAllVocabularies();
    }

    @Override
    public List<VocabularyStatusDTO> getAllVocabularyStatuses() {
        List<Vocabulary> vocabularies = getAllVocabularies();
        List<VocabularyStatusDTO> result = new ArrayList<VocabularyStatusDTO>();
        for (Vocabulary current : vocabularies){
            result.add(new VocabularyStatusDTO(current));
        }
        return result;
    }

}
