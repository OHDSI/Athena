/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.athena.model.security.AthenaUser;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.mail.MessagingException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FailedSavingSender extends MailSender {

    public FailedSavingSender(JavaMailSender mailSender, MailContentBuilder contentBuilder) {

        super(mailSender, contentBuilder);
    }

    public void send(AthenaUser user, Map<String, Object> parameters) {

        try {
            super.send(user.getEmail(), parameters);
        } catch (MessagingException | MailException | UnsupportedEncodingException ignored) {
            // no operation
        }
    }


    @Override
    public String getSubject() {

        return "Failed vocabularies saving";
    }

    @Override
    public String getTemplateName() {

        return "mail/failed_vocabularies_download";
    }

}
