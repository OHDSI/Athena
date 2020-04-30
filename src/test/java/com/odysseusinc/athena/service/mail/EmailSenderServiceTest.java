package com.odysseusinc.athena.service.mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailSenderServiceTest {

    private final String from = "test.from.account@localhost";
    private final String notifier = "Test";
    private final String testAccount = "test.to.account@localhost";
    private final String subject = "subject";
    private final String emailBody = "BODY";

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MimeMessage mimeMessage;

    private EmailSenderService emailSenderService;
    private Map<String, Object> params = new HashMap<>();

    @Before
    public void setUp() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSenderService = new EmailSenderService(mailSender, notifier, from);
    }

    @Test
    public void itShouldNotSentEmailWithoutRecepients() {

        emailSenderService.sendAsync(subject, emailBody, emptyList(), emptyList());

        verify(mailSender, never()).send((MimeMessage) any());
    }


    @Test
    public void itShouldSendEmailToTheRecipient() {

        emailSenderService.sendAsync(subject, emailBody, Arrays.asList(testAccount), emptyList());

        verify(mailSender).send((MimeMessage) any());
    }
}