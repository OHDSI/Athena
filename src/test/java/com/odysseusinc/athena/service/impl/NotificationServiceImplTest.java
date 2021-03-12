package com.odysseusinc.athena.service.impl;


import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.repositories.v5.VocabularyRepository;
import com.odysseusinc.athena.service.mail.EmailService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    private final String code = "VOCAB_CODE";
    //Fri Jul 02 2021 23:30:00 GMT+0000
    private final long july02Millis = 1625268600L * 1000;
    //Sat Jul 03 2021 00:30:00 GMT+0000
    private final long july03Millis = 1625272200L * 1000;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserService userService;
    @Mock
    private VocabularyConversionRepository vocabularyConversionRepository;
    @Mock
    private VocabularyRepository vocabularyRepository;
    @Mock
    private VocabularyV5 vocabularyV5;
    @Captor
    private ArgumentCaptor<Notification> captor;
    @InjectMocks
    private NotificationServiceImpl notificationService;
    private Notification subscription;
    private VocabularyConversion conversion;

    @Before
    public void setUp() {
        subscription = new Notification();
        subscription.setVocabularyCode(code);
        when(notificationRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(subscription));
        conversion = new VocabularyConversion();
        conversion.setIdV5(code);
        conversion.setLatestUpdate(new Date(july03Millis));
        when(vocabularyConversionRepository.findByLatestUpdateIsNotNull()).thenReturn(Arrays.asList(conversion));

        when(vocabularyRepository.findByIdIn(any())).thenReturn(Arrays.asList(vocabularyV5));
    }

    @Test
    public void shouldOnlyUpdateVersionFromPreviousFormat() {

        subscription.setActualVersion("RxNorm 20200504");
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
    public void shouldNotifySubscriberAndUpdateSubscriptionIfVersionHasChangedFromNullToValue() {

        subscription.setActualVersion(null);
        notificationService.processUsersVocabularyUpdateSubscriptions(-1L);

        assertThat(subscription.getActualVersion()).isEqualTo("03-Jul-2021");
        verify(emailService, times(1)).sendVocabularyUpdateNotification(any(), any());
    }

    @Test
    public void shouldCreateVocabularySubscriptionWhenLatestUpdateIsNull() {

        when(notificationRepository.findByUserId(any())).thenReturn(Lists.emptyList());
        when(vocabularyConversionRepository.findByIdV5(any())).thenReturn(new VocabularyConversion());

        notificationService.createSubscriptions(-1L, new String[]{code});

        verify(notificationRepository, times(1)).save(captor.capture());
        Notification newNotification = captor.getValue();
        assertThat(newNotification.getActualVersion()).isNull();
    }

    @Test
    public void shouldCreateVocabularySubscriptionWhenLatestUpdateIsSet() {
        VocabularyConversion conversion = new VocabularyConversion();
        conversion.setLatestUpdate(new Date(july02Millis));
        when(notificationRepository.findByUserId(any())).thenReturn(Lists.emptyList());
        when(vocabularyConversionRepository.findByIdV5(any())).thenReturn(conversion);

        notificationService.createSubscriptions(-1L, new String[]{code});

        verify(notificationRepository, times(1)).save(captor.capture());
        Notification newNotification = captor.getValue();
        assertThat(newNotification.getActualVersion()).isEqualTo("02-Jul-2021");
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