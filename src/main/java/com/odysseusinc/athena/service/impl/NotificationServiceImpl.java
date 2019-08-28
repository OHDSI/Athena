package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.repositories.v5.VocabularyRepository;
import com.odysseusinc.athena.service.NotificationService;
import com.odysseusinc.athena.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final VocabularyConversionRepository vocabularyConversionRepository;
    private final VocabularyRepository vocabularyRepository;

    @Autowired
    public NotificationServiceImpl(EmailService emailService, NotificationRepository notificationRepository, UserService userService, VocabularyConversionRepository vocabularyConversionRepository, VocabularyRepository vocabularyRepository) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.vocabularyConversionRepository = vocabularyConversionRepository;
        this.vocabularyRepository = vocabularyRepository;
    }

    @Override
    public void createSubscriptions(Long userId, String[] vocabularyCodes) {

        for (String vocabularyCode : vocabularyCodes) {

            if (!notificationRepository.findByUserIdAndVocabularyCode(userId, vocabularyCode).isPresent()) {
                final VocabularyV5 vocabulary = vocabularyRepository.findOne(vocabularyCode);
                if (vocabulary != null) {
                    final VocabularyConversion vocabularyConversion = vocabularyConversionRepository.findByIdV5(vocabularyCode);
                    final Notification newNotification = new Notification(userId, vocabularyConversion, vocabularyCode, vocabulary.getVersion());
                    notificationRepository.save(newNotification);
                }
            }
        }
    }

    @Override
    public void deleteSubscription(Long userId, String vocabularyCode) {
        notificationRepository.findByUserIdAndVocabularyCode(userId, vocabularyCode)
                .ifPresent(notificationRepository::delete);
    }

    /**
     * this method is required for the transition of already existing subscriptions
     */
    @Override
    public void ensureVocabularyVersionAndCodeAreSet() {

        Map<String, String> vocabularyVersionMap = buildVocabularyVersionMap();

        final List<Notification> existingNotifications = notificationRepository.findByVocabularyCodeIsNullOrActualVersionIsNull();

        for (Notification notification : existingNotifications) {
            final String vocabularyCode = notification.getVocabularyConversion().getIdV5();
            notification.setVocabularyCode(vocabularyCode);
            final String actualVersion = vocabularyVersionMap.get(vocabularyCode);
            notification.setActualVersion(actualVersion);
        }
    }

    @Override
    public void processUsersVocabularyUpdateSubscriptions(Long userId) {

        Map<String, String> vocabularyVersionMap = buildVocabularyVersionMap();
        final List<Notification> notificationsSubscriptions = notificationRepository.findByUserId(userId);

        List<Notification> changedSubscriptions = new ArrayList<>();
        for (Notification notification : notificationsSubscriptions) {
            final String vocabularyCode = notification.getVocabularyCode();
            if (!equalsIgnoreCase(notification.getActualVersion(), vocabularyVersionMap.get(vocabularyCode))) {
                changedSubscriptions.add(notification);
            }
        }

        if (!changedSubscriptions.isEmpty()) {
            emailService.sendVocabularyUpdateNotification(userService.get(userId), changedSubscriptions);

            for (Notification notification : changedSubscriptions) {
                String theLatestVersion = vocabularyVersionMap.get(notification.getVocabularyCode());
                notification.setActualVersion(theLatestVersion);
            }
        }

    }

    private Map<String, String> buildVocabularyVersionMap() {
        final List<VocabularyV5> allVersions = vocabularyRepository.findAll();

        return allVersions.stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getVersion()), HashMap::putAll);
    }
}
