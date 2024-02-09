package com.odysseusinc.athena.service.mail;

import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.exceptions.AthenaException;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.util.CDMVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    @Mock
    private EmailSenderService emailSenderService;
    @Mock
    private MailContentBuilder contentBuilder;
    @Mock
    private UrlBuilder urlBuilder;
    @Mock
    private UserService userService;
    @Mock
    private AthenaUser athenaUser;
    @Mock
    private AthenaUser athenaAdmin;
    private EmailRecipients recipients;

    @InjectMocks
    private EmailServiceImpl emailService;

    private String TEST_EMAIL = "dummy@email.com";
    private String TEST_ADMIN_EMAIL = "dummyadmin@email.com";


    @Before
    public void setUp() {

        when(contentBuilder.build(any(), any())).thenReturn(EMPTY);
        when(athenaUser.getEmail()).thenReturn(TEST_EMAIL);
        when(athenaAdmin.getEmail()).thenReturn(TEST_ADMIN_EMAIL);

        when(userService.getAdmins()).thenReturn(Arrays.asList(athenaAdmin));

        recipients = EmailRecipients.builder().to(Arrays.asList(TEST_EMAIL)).build();
    }

    @Test
    public void shouldSendVocabularyDownloadLink() {
        when(emailSenderService.sendAsync(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        emailService.sendVocabularyDownloadLink(athenaUser, EMPTY, EMPTY, CDMVersion.V5, new HashMap<>(), EMPTY);

        verify(emailSenderService).sendAsync(EmailType.VOCABULARIES_LINK.getSubject(), EMPTY, recipients);
    }


    @Test
    public void shouldNotifyAdminsOnVocabularyDownloadLinkEmailSendingFailure() {

        CompletableFuture<Void> failureAction = CompletableFuture.runAsync(() -> {
            throw new AthenaException();
        });
        when(emailSenderService.sendAsync(any(), any(), any())).thenReturn(failureAction);

        emailService.sendVocabularyDownloadLink(athenaUser, EMPTY, EMPTY, CDMVersion.V5, new HashMap<>(), EMPTY);

        verify(emailSenderService).sendAsync(EmailType.VOCABULARIES_LINK.getSubject(), EMPTY, recipients);
        verify(emailSenderService).sendAsync(EmailType.FAILED_SENDING_TO_ADMIN.getSubject(), EMPTY, EmailRecipients.builder().to(Arrays.asList(TEST_ADMIN_EMAIL)).build());
    }

}
