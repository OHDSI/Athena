package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.repositories.v5.VocabularyRepository;
import com.odysseusinc.athena.service.NotificationService;
import com.odysseusinc.athena.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    public static final DateTimeFormatter LATEST_UPDATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
            .withZone(ZoneOffset.UTC);
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

        final String[] requestedVocabularies = Arrays.stream(vocabularyCodes)
                .filter(Objects::nonNull)
                .distinct()
                .toArray(String[]::new);

        vocabularyRepository.findByIdIn(requestedVocabularies).forEach(vocabulary -> {
            final VocabularyConversion vocabularyConversion = vocabularyConversionRepository.findByIdV5(vocabulary.getId());
            String theLatestVersion = null;
            if(vocabularyConversion.getLatestUpdate()!=null) {
                theLatestVersion = LATEST_UPDATE_FORMATTER.format(toOffsetDateTime(vocabularyConversion.getLatestUpdate()));
            }
            notificationRepository.insertIfAbsent(
                    userId,
                    vocabularyConversion.getIdV4(),
                    vocabulary.getId(),
                    theLatestVersion);
        });
    }

    @Override
    public void deleteSubscription(Long userId, String vocabularyCode) {
        notificationRepository.findByUserIdAndVocabularyCode(userId, vocabularyCode)
                .ifPresent(notificationRepository::delete);
    }

    @Override
    public void deleteAllSubscriptions(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void processUsersVocabularyUpdateSubscriptions(Long userId) {

        Map<String, OffsetDateTime> vocabularyVersionMap = buildVocabularyVersionMap();
        final List<Notification> notificationsSubscriptions = notificationRepository.findByUserId(userId);

        List<Notification> changedSubscriptions = new ArrayList<>();
        for (Notification notification : notificationsSubscriptions) {
            final String vocabularyCode = notification.getVocabularyCode();
            OffsetDateTime latestUpdate = vocabularyVersionMap.getOrDefault(vocabularyCode, null);
            String theLatestVersion = isNull(latestUpdate) ? null : LATEST_UPDATE_FORMATTER.format(latestUpdate);
            if (notification.getActualVersion()!=null && !isPreviousVersionSet(notification)) {
                notification.setActualVersion(theLatestVersion);
            } else if (!equalsIgnoreCase(notification.getActualVersion(), theLatestVersion)) {
                notification.setActualVersion(theLatestVersion);
                changedSubscriptions.add(notification);
            }
        }

        if (!changedSubscriptions.isEmpty()) {
            emailService.sendVocabularyUpdateNotification(userService.get(userId), changedSubscriptions);
        }
    }

    private boolean isPreviousVersionSet(Notification notification) {
        try {
            TemporalAccessor previousVersion = LATEST_UPDATE_FORMATTER.parse(notification.getActualVersion());
            return previousVersion != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private Map<String, OffsetDateTime> buildVocabularyVersionMap() {
        return vocabularyConversionRepository.findByLatestUpdateIsNotNull().stream()
                .collect(HashMap::new, (map, conversion) -> map.put(conversion.getIdV5(), toOffsetDateTime(conversion.getLatestUpdate())), HashMap::putAll);
    }

    private OffsetDateTime toOffsetDateTime(Date sqlDate) {
        return Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneOffset.UTC).toOffsetDateTime();
    }
}
