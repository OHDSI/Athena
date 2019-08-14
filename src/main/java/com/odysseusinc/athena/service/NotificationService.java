package com.odysseusinc.athena.service;

import java.util.Map;

public interface NotificationService {

    Map<String, String> buildVocabularyVersionMap();

    void ensureVocabularyVersionAndCodeAreSet();

    void processUsersVocabularyUpdateSubscriptions(Long userId, Map<String,String> vocabularyVersionMap);

    void createSubscriptions(Long userId, String[] vocabularyCodes);

    void deleteSubscription(Long userId, String vocabularyCode);
}
