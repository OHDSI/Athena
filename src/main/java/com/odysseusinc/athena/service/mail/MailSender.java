/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.service.mail;

import static java.text.MessageFormat.format;

import com.odysseusinc.athena.model.security.AthenaUser;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public abstract class MailSender {
    private static Logger LOGGER = LoggerFactory.getLogger(MailSender.class);

    private final JavaMailSender mailSender;
    private final MailContentBuilder contentBuilder;

    @Value("${athena.mail.notifier}")
    private String notifier;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    public MailSender(JavaMailSender mailSender, MailContentBuilder contentBuilder) {

        this.mailSender = mailSender;
        this.contentBuilder = contentBuilder;
    }

    public void send(AthenaUser user, Map<String, Object> parameters) throws MessagingException,
            UnsupportedEncodingException {

        send(user.getEmail(), parameters);
    }

    public void send(String email, Map<String, Object> parameters) throws MessagingException, MailException,
            UnsupportedEncodingException {

        if (email == null) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(getSubject());
            helper.setFrom(from, notifier);
            helper.setTo(email);
            final String emailBody = contentBuilder.build(getTemplateName(), parameters);
            helper.setText(emailBody, true);

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            LOGGER.error(format("{0}[user email [{1}], subject [{2}]]", ex.getMessage(), email,
                    getSubject()), ex);
            throw ex;
        }
    }

    public void send(List<String> emails, Map<String, Object> parameters) throws MessagingException, MailException,
            UnsupportedEncodingException {

        for (String email : emails) {
            send(email, parameters);
        }
    }

    protected abstract String getSubject();

    protected abstract String getTemplateName();

}
