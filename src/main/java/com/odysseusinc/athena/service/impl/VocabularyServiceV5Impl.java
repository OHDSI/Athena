package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import com.odysseusinc.athena.repositories.v5.VocabularyRepository;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.odysseusinc.athena.model.common.AthenaConstants.OMOP_VOCABULARY_ID;

@Service
@Transactional(transactionManager = "athenaV5TransactionManager")
public class VocabularyServiceV5Impl implements VocabularyServiceV5 {

    private static final Logger log = LoggerFactory.getLogger(VocabularyServiceV5Impl.class);

    private final VocabularyRepository vocabularyRepository;

    public VocabularyServiceV5Impl(VocabularyRepository vocabularyRepository) {

        this.vocabularyRepository = vocabularyRepository;
    }

    @Override
    public String getOMOPVocabularyVersion() {

        final VocabularyV5 omopVocabulary = vocabularyRepository.getOne(OMOP_VOCABULARY_ID);
        if (omopVocabulary != null) {
            log.debug("Current OMOP Vocabulary: {} {}: {}", omopVocabulary.getId(), omopVocabulary.getName(), omopVocabulary.getVersion());

            return omopVocabulary.getVersion();
        }
        log.warn("OMOP Vocabulary not found");
        return null;
    }
}