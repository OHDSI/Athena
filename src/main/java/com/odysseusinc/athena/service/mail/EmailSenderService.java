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

import com.opencsv.util.StringUtils;
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
import java.util.Map;

@Service
public class EmailSenderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderService.class);

    private final MailContentBuilder contentBuilder;
    private final JavaMailSender mailSender;

    @Value("${athena.mail.notifier}")
    private String notifier;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    public EmailSenderService(MailContentBuilder contentBuilder, JavaMailSender mailSender) {

        this.contentBuilder = contentBuilder;
        this.mailSender = mailSender;
    }

    public void send(EmailType messageType, Map<String, Object> parameters, String... emails) {

        if (emails == null || emails.length == 0 || StringUtils.isBlank(emails[0])) {
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            final String emailBody = contentBuilder.build(messageType.getTemplate(), parameters);
            LOGGER.debug("Sending  [{}] email: \n\n\n{}\n\n\n", messageType, emailBody);
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(messageType.getSubject());
            helper.setFrom(from, notifier);
            helper.setTo(emails);
            helper.setText(emailBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            LOGGER.error("{} [user email: {}, subject: {}]", ex.getMessage(), emails, messageType.getSubject(), ex);
            throw new RuntimeException(ex);
        }
    }

}
