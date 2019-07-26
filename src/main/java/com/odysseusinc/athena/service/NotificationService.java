package com.odysseusinc.athena.service;

import java.util.Map;

public interface NotificationService {

    Map<String, String> buildVocabularyVersionMap();

    void ensureVocabularyVersionAndCodeAreSet();

    void processUsersVocabularyUpdateSubscriptions(Long userId, Map<String,String> vocabularyVersionMap);

    void updateNotificationSubscriptions(Long userId, String[] vocabularyCodes, boolean notify);
}
