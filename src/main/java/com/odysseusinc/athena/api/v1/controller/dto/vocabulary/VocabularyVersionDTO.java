package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

public class VocabularyVersionDTO {

    private final String vocabularyVersion;

    public VocabularyVersionDTO(String vocabularyVersion) {

        this.vocabularyVersion = vocabularyVersion;
    }

    public String getVocabularyVersion() {
        return vocabularyVersion;
    }


}
