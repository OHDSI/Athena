package com.odysseusinc.athena.service;

import java.util.Map;

public interface NotificationService {

    void ensureVocabularyVersionAndCodeAreSet();

    void processUsersVocabularyUpdateSubscriptions(Long userId);

    void createSubscriptions(Long userId, String[] vocabularyCodes);

    void deleteSubscription(Long userId, String vocabularyCode);
}
