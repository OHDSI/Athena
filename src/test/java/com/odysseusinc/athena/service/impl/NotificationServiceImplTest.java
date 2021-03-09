package com.odysseusinc.athena.service.impl;


import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.service.mail.EmailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    @Mock
    private EmailService emailService;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserService userService;
    @Mock
    private VocabularyConversionRepository vocabularyConversionRepository;
    @InjectMocks
    private NotificationServiceImpl notificationService;
    private Notification subscription;
    private String code = "VOCAB_CODE";
    private VocabularyConversion conversion;
    //Fri Jul 02 2021 23:30:00 GMT+0000
    private long july02Millis = 1625268600L * 1000;
    //Sat Jul 03 2021 00:30:00 GMT+0000
    private long july03Millis = 1625272200L * 1000;

    @Before
    public void setUp() {
        subscription = new Notification();
        subscription.setVocabularyCode(code);
        when(notificationRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(subscription));
        conversion = new VocabularyConversion();
        conversion.setIdV5(code);
        conversion.setLatestUpdate( new Date(july03Millis));
        when(vocabularyConversionRepository.findByLatestUpdateIsNotNull()).thenReturn(Arrays.asList(conversion));
    }

    @Test
    public void shouldOnlyUpdateVersionIfWasNotSetBefore() {

        subscription.setActualVersion(null);
        notificationService.processUsersVocabularyUpdateSubscriptions(-1L);

        assertThat(subscription.getActualVersion()).isEqualTo("03-Jul-2021");
        verify(emailService, never()).sendVocabularyUpdateNotification(any(), any());
    }

    @Test
    public void shouldOnlyUpdateVersionIfWasNotImproperlySetBefore() {

        subscription.setActualVersion("wrong-format");
        notificationService.processUsersVocabularyUpdateSubscriptions(-1L);

        assertThat(subscription.getActualVersion()).isEqualTo("03-Jul-2021");
        verify(emailService, never()).sendVocabularyUpdateNotification(any(), any());
    }

    @Test
    public void shouldSkipSubscriptionIfVersionDoesNotChange() {

        subscription.setActualVersion("03-Jul-2021");
        notificationService.processUsersVocabularyUpdateSubscriptions(-1L);

        assertThat(subscription.getActualVersion()).isEqualTo("03-Jul-2021");
        verify(emailService, never()).sendVocabularyUpdateNotification(any(), any());
    }
    @Test
    public void shouldNotifySubscriberAndUpdateSubscriptionIfVersionHasChanged() {

        subscription.setActualVersion("02-Jul-2021");
        notificationService.processUsersVocabularyUpdateSubscriptions(-1L);

        assertThat(subscription.getActualVersion()).isEqualTo("03-Jul-2021");
        verify(emailService, times(1)).sendVocabularyUpdateNotification(any(), any());
    }

    @Test
    public void shouldUseAthenaLatestUpdateFormat() {
        LocalDate date = LocalDate.of(2015, 7, 15);
        String version = NotificationServiceImpl.LATEST_UPDATE_FORMATTER.format(date);
        assertThat(version).isEqualTo("15-Jul-2015");
    }

    @Test
    public void shouldHandleDateChangeProperlyUTC() {
        Date sqlDate = new Date(july02Millis);
        OffsetDateTime localDateTime = Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneOffset.UTC).toOffsetDateTime();
        String version = NotificationServiceImpl.LATEST_UPDATE_FORMATTER.format(localDateTime);
        assertThat(version).isEqualTo("02-Jul-2021");
    }

    @Test
    public void shouldHandleDateChangeProperlyUTCTo() {
        Date sqlDate = new Date(july03Millis);
        OffsetDateTime localDateTime = Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneOffset.UTC).toOffsetDateTime();
        String version = NotificationServiceImpl.LATEST_UPDATE_FORMATTER.format(localDateTime);
        assertThat(version).isEqualTo("03-Jul-2021");
    }

    @Test
    public void shouldHandleDateChangeESTProperly() {
        Date sqlDate = new Date(july02Millis);
        OffsetDateTime dateTime = Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneId.of("America/New_York")).toOffsetDateTime();
        String version = NotificationServiceImpl.LATEST_UPDATE_FORMATTER.format(dateTime);
        assertThat(version).isEqualTo("02-Jul-2021");
    }

    @Test
    public void shouldHandleDateChangeProperlyESTTo() {
        Date sqlDate = new Date(july03Millis);
        OffsetDateTime dateTime = Instant.ofEpochMilli(sqlDate.getTime()).atZone(ZoneId.of("America/New_York")).toOffsetDateTime();
        String version = NotificationServiceImpl.LATEST_UPDATE_FORMATTER.format(dateTime);
        assertThat(version).isEqualTo("03-Jul-2021");
    }

}