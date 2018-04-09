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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.service.impl.UserService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LicenseRequestSender extends ToAdminsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseRequestSender.class);

    private FailedSendingToAdminSender failedSendingToAdminSender;
    private UrlBuilder urlBuilder;

    @Autowired
    public LicenseRequestSender(JavaMailSender mailSender, MailContentBuilder contentBuilder, UserService userService,
                                FailedSendingToAdminSender failedSendingToAdminSender,
                                UrlBuilder urlBuilder) {

        super(mailSender, contentBuilder, userService);
        this.failedSendingToAdminSender = failedSendingToAdminSender;
        this.urlBuilder = urlBuilder;
    }

    public void sendToAdmins(License license) {

        try {
            super.sendToAdmins(license);
        } catch (Exception ex) {
            failedSendingToAdminSender.send(ex);
        }
    }

    @Override
    public String getSubject() {

        return "License request";
    }

    @Override
    public String getTemplateName() {

        return "mail/license_request";
    }

    @Override
    protected Map<String, Object> getParameters(Object object) {

        Map<String, Object> parameters = new HashMap<>();
        License license = (License) object;
        parameters.put("username", license.getUser().getFirstName() + ' ' + license.getUser().getLastName());
        parameters.put("email", license.getUser().getEmail());
        parameters.put("vocabularyname", license.getVocabularyConversion().getName());
        parameters.put("approveUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), TRUE, license.getToken()));
        parameters.put("declineUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), FALSE, license.getToken()));
        return parameters;
    }

}
