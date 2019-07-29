package com.odysseusinc.athena.service;

import java.util.Map;

public interface NotificationService {

    Map<String, String> buildVocabularyVersionMap();

    void ensureVocabularyVersionAndCodeAreSet();

    void processUsersVocabularyUpdateSubscriptions(Long userId, Map<String,String> vocabularyVersionMap);

    void createNotificationSubscriptions(Long userId, String[] vocabularyCodes);

    void deleteNotificationSubscription(Long userId, String vocabularyCode);
}
