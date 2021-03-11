package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
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

        for (Long subscribedUserId : notificationRepository.getSubscribedUserIds()) {
            try {
                notificationService.processUsersVocabularyUpdateSubscriptions(subscribedUserId);
            } catch (Exception ex) {
                log.error("notifications processing failure for the userId: {}", subscribedUserId, ex);
            }
        }
    }
}
