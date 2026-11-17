package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.exceptions.LicenseException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.service.DownloadBundleService.BundleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The share notification must only carry the direct archive link to recipients who hold the
 * licences the bundle needs; everyone else is pointed at the download page instead.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShareNotificationLinkTest {

    private static final long BUNDLE_ID = 7L;
    private static final String UUID = "bundle-uuid";
    private static final String RECIPIENT_EMAIL = "recipient@example.org";
    private static final String ZIP_LINK = "https://athena.example/api/v1/vocabularies/zip/" + UUID;
    private static final String PAGE_LINK = "https://athena.example/vocabulary/list";

    @InjectMocks
    private DownloadShareServiceImpl service;

    @Mock
    private DownloadBundleService downloadBundleService;
    @Mock
    private DownloadBundleRepository downloadBundleRepository;
    @Mock
    private DownloadShareRepository downloadShareRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;
    @Mock
    private UrlBuilder urlBuilder;
    @Mock
    private VocabularyService vocabularyService;

    private final AthenaUser owner = user(1L);
    private final AthenaUser recipient = user(2L);

    @BeforeEach
    void setUp() {

        DownloadBundle bundle = new DownloadBundle();
        bundle.setId(BUNDLE_ID);
        bundle.setUuid(UUID);
        when(downloadBundleRepository.getOne(BUNDLE_ID)).thenReturn(bundle);
        when(downloadShareRepository.findByBundle(bundle)).thenReturn(Collections.emptyList());
        when(downloadBundleService.getType(bundle)).thenReturn(BundleType.V5);
        when(userService.getUser(RECIPIENT_EMAIL)).thenReturn(recipient);
        when(urlBuilder.downloadVocabulariesLink(UUID)).thenReturn(ZIP_LINK);
        when(urlBuilder.downloadVocabulariesPageUrl()).thenReturn(PAGE_LINK);
    }

    @Test
    void licensedRecipientGetsDirectLink() {

        service.change(BUNDLE_ID, RECIPIENT_EMAIL, owner);

        verify(emailService).sendVocabulariesWereSharedNotification(
                eq(recipient), eq(owner), eq(ZIP_LINK), any(), any());
    }

    @Test
    void unlicensedRecipientGetsDownloadPageInstead() {

        doThrow(new LicenseException("missing", List.of(1)))
                .when(vocabularyService).checkBundleVocabularies(BUNDLE_ID, recipient.getId());

        service.change(BUNDLE_ID, RECIPIENT_EMAIL, owner);

        verify(emailService).sendVocabulariesWereSharedNotification(
                eq(recipient), eq(owner), eq(PAGE_LINK), any(), any());
    }

    private static AthenaUser user(long id) {

        AthenaUser user = new AthenaUser();
        user.setId(id);
        return user;
    }
}
