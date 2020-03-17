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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.toArray;
import static java.util.Collections.emptyList;

@Service
public class EmailSenderService {
    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final MailContentBuilder contentBuilder;
    private final String from;
    private final String notifier;

    @Autowired
    public EmailSenderService(MailContentBuilder contentBuilder,
                              JavaMailSender mailSender,
                              @Value("${athena.mail.notifier}") String notifier,
                              @Value("${spring.mail.username}") String from) {

        this.contentBuilder = contentBuilder;
        this.mailSender = mailSender;
        this.notifier = notifier;
        this.from = from;
    }

    public void send(EmailType messageType, Map<String, Object> parameters, String... toEmails) {

        this.send(messageType, parameters, Arrays.asList(toEmails), emptyList());
    }


    public void send(EmailType messageType, Map<String, Object> parameters, List<String> toEmails, List<String> bccEmails) {

        if (CollectionUtils.isEmpty(toEmails) && CollectionUtils.isEmpty(bccEmails)) {
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            final String emailBody = contentBuilder.build(messageType.getTemplate(), parameters);
            log.debug("Sending  [{}] email: \n\n\n{}\n\n\n", messageType, emailBody);
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(messageType.getSubject());
            helper.setFrom(from, notifier);
            helper.setTo(toArray(toEmails, String.class));
            helper.setBcc(toArray(bccEmails, String.class));
            helper.setText(emailBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("{} [user email: {}, subject: {}]", ex.getMessage(), String.join(",", toEmails), messageType.getSubject(), ex);
            throw new AthenaException();
        }
    }
}
