package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VocabularyUpdatesChecker {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Autowired
    public VocabularyUpdatesChecker(NotificationRepository notificationRepository, NotificationService notificationService) {

        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }


    public void sendVocabularyUpdatesNotification() {

        notificationService.ensureVocabularyVersionAndCodeAreSet();

        for (Long subscribedUserId : notificationRepository.getSubscribedUserIds()) {
            notificationService.processUsersVocabularyUpdateSubscriptions(subscribedUserId);
        }
    }
}
