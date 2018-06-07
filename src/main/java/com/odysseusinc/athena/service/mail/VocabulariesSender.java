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
import com.odysseusinc.athena.util.CDMVersion;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VocabulariesSender extends MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabulariesSender.class);

    @Autowired
    public VocabulariesSender(JavaMailSender mailSender, MailContentBuilder contentBuilder,
                              FailedSendingToAdminSender failedSendingToAdminSender) {

        super(mailSender, contentBuilder);
        this.failedSendingToAdminSender = failedSendingToAdminSender;
    }

    @Value("${vocabularies.download.control.files.url}")
    private String controlFilesUrl;
    @Value("${vocabularies.download.forum.url}")
    private String forumUrl;
    @Value("${vocabularies.download.umls.url}")
    private String umlsUrl;

    private FailedSendingToAdminSender failedSendingToAdminSender;

    public void send(AthenaUser user, String url, CDMVersion version) throws MessagingException, MailException {

        try {
            super.send(user, getParameters(url, version));
            LOGGER.info("Email with link for download zip is sent to user with id: [{}], zip link: [{}]",
                    user.getId(), url);

        } catch (Exception ex) {
            failedSendingToAdminSender.send(ex);
        }
    }

    @Override
    public String getSubject() {

        return "OMOP Vocabularies. Your download link";
    }

    @Override
    public String getTemplateName() {

        return "mail/vocabularies_download";
    }

    private Map<String, Object> getParameters(String url, CDMVersion version) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("forumUrl", forumUrl);
        parameters.put("controlFilesUrl", controlFilesUrl);
        parameters.put("url", url);
        parameters.put("umlsUrl", umlsUrl);
        parameters.put("version", String.valueOf((int) version.getValue()));
        return parameters;
    }


}
