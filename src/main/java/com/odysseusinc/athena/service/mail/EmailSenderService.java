/*
 *
 * Copyright 2019 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Alexandr Cumarav
 * Created: July 27, 2019
 *
 */

package com.odysseusinc.athena.service.mail;

import com.odysseusinc.athena.exceptions.AthenaException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Iterables.toArray;
import static com.odysseusinc.athena.model.common.AthenaConstants.FIVE_SEC_MS;

@Service
public class EmailSenderService {
    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String notifier;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender,
                              @Value("${athena.mail.notifier}") String notifier,
                              @Value("${spring.mail.username}") String from) {

        this.mailSender = mailSender;
        this.notifier = notifier;
        this.from = from;
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = FIVE_SEC_MS, multiplier = 3))
    @Async("emailSenderExecutor")
    public CompletableFuture<Void> sendAsync(String subject, String emailBody, EmailRecipients recipients) {

        if (CollectionUtils.isNotEmpty(recipients.getTo()) || CollectionUtils.isNotEmpty(recipients.getBcc())) {
            sendMessage(subject, emailBody, recipients);
        }
        return CompletableFuture.completedFuture(null);
    }

    private boolean sendMessage(String subject, String emailBody, EmailRecipients recipients) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            log.debug("Sending  [{}] email: \n\n\n{}\n\n\n", subject, emailBody);
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(subject);
            helper.setFrom(from, notifier);
            if (StringUtils.isNotBlank(recipients.getReplyTo())) {
                helper.setReplyTo(recipients.getReplyTo());
            }
            helper.setTo(toArray(recipients.getTo(), String.class));
            helper.setBcc(toArray(recipients.getBcc(), String.class));
            helper.setText(emailBody, true);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            throw new AthenaException("Send Message issue", ex);
        }
    }
}
