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

import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.model.security.AthenaUser;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LicenseAcceptanceSender extends MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabulariesSender.class);

    private UrlBuilder urlBuilder;

    private FailedSendingToAdminSender failedSendingToAdminSender;

    @Autowired
    public LicenseAcceptanceSender(JavaMailSender mailSender, MailContentBuilder contentBuilder,
                                   UrlBuilder urlBuilder,
                                   FailedSendingToAdminSender failedSendingToAdminSender) {

        super(mailSender, contentBuilder);
        this.failedSendingToAdminSender = failedSendingToAdminSender;
        this.urlBuilder = urlBuilder;
    }

    public void send(AthenaUser user, boolean accepted, String vocabularyName) {

        try {
            super.send(user, getParameters(accepted, vocabularyName));
            LOGGER.info("Notification with acceptance solution [{}] is sent to user with id: [{}]",
                    accepted, user.getId());

        } catch (Exception ex) {
            failedSendingToAdminSender.send(ex);
        }
    }

    @Override
    public String getSubject() {

        return "Licensing acceptance";
    }

    @Override
    public String getTemplateName() {

        return "mail/license_acceptance";
    }

    private Map<String, Object> getParameters(@NotNull Boolean accepted, String vocabularyName) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("downloadVocabulariesPageUrl", urlBuilder.downloadVocabulariesPageUrl());
        parameters.put("accepted", accepted);
        parameters.put("vocabularyName", vocabularyName);
        return parameters;
    }

}
