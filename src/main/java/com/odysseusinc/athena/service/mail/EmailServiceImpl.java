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

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.util.CDMVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Transactional(readOnly = true)
@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailSenderService emailSenderService;
    private final UrlBuilder urlBuilder;
    private final UserService userService;

    @Value("${vocabularies.download.control.files.url}")
    private String controlFilesUrl;
    @Value("${vocabularies.download.forum.url}")
    private String forumUrl;
    @Value("${vocabularies.download.umls.url}")
    private String umlsUrl;

    public EmailServiceImpl(EmailSenderService emailSenderService, UrlBuilder urlBuilder, UserService userService) {
        this.emailSenderService = emailSenderService;
        this.urlBuilder = urlBuilder;
        this.userService = userService;
    }

    @Override
    public void sendVocabularyUpdateNotification(AthenaUser user, List<Notification> updatedVocabularies) {

        Map<String, String> vocabularyDetails = new HashMap<>();
        updatedVocabularies.forEach(v -> vocabularyDetails.put(v.getVocabularyCode(), v.getVocabularyConversion().getName()));

        emailSenderService.send(EmailType.VOCABULARIES_UPDATE_NOTIFICATION, ImmutableMap.of("UPDATED_VOCABULARIES", vocabularyDetails), user.getEmail());
    }

    @Override
    public void sendVocabularyDownloadLink(AthenaUser user, String url, CDMVersion version, String vocabularyReleaseVersion) {

        try {
            emailSenderService.send(EmailType.VOCABULARIES_LINK, buildParameters(url, version, vocabularyReleaseVersion), user.getEmail());
            LOGGER.info("Email with link for download zip is sent to user with id: [{}], zip link: [{}]",
                    user.getId(), url);

        } catch (Exception ex) {
            emailSenderService.send(EmailType.FAILED_SENDING_TO_ADMIN, getParameters(ex), getAdminEmails());
        }
    }

    @Override
    public void sendFailedSaving(AthenaUser user) {

        emailSenderService.send(EmailType.FAILED_SAVING, Collections.emptyMap(), user.getEmail());
    }

    @Override
    public void sendLicenseRequestToAdmins(License license) {

        final Map<String, Object> licenceRequestEmailParametes = getParameters(license);

        try {
            emailSenderService.send(EmailType.LICENSE_REQUEST, licenceRequestEmailParametes, getAdminEmails());
        } catch (Exception ex) {
            emailSenderService.send(EmailType.FAILED_SENDING_TO_ADMIN, getParameters(ex), getAdminEmails());
        }
    }

    @Override
    public void sendLicenseAcceptance(AthenaUser user, boolean accepted, String vocabularyName) {

        try {
            emailSenderService.send(EmailType.LICENSE_ACCEPTANCE, getParameters(accepted, vocabularyName), user.getEmail());
            LOGGER.info("Notification with acceptance solution [{}] is sent to user with id: [{}]",
                    accepted, user.getId());

        } catch (Exception ex) {
            emailSenderService.send(EmailType.FAILED_SENDING_TO_ADMIN, getParameters(ex), getAdminEmails());
        }
    }

    private String[] getAdminEmails() {

        return userService.getAdmins().stream()
                .map(AthenaUser::getEmail).toArray(size -> new String[size]);
    }

    private Map<String, Object> buildParameters(String url, CDMVersion version, String vocabularyReleaseVersion) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("forumUrl", forumUrl);
        parameters.put("controlFilesUrl", controlFilesUrl);
        parameters.put("url", url);
        parameters.put("umlsUrl", umlsUrl);
        parameters.put("version", String.valueOf((int) version.getValue()));
        parameters.put("vocabularyReleaseVersion", vocabularyReleaseVersion);
        return parameters;
    }

    private Map<String, Object> getParameters(@NotNull Boolean accepted, String vocabularyName) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("downloadVocabulariesPageUrl", urlBuilder.downloadVocabulariesPageUrl());
        parameters.put("accepted", accepted);
        parameters.put("vocabularyName", vocabularyName);
        return parameters;
    }

    private Map<String, Object> getParameters(Exception exception) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("exception", exception.toString());
        return parameters;
    }

    private Map<String, Object> getParameters(License license) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", license.getUser().getFirstName() + ' ' + license.getUser().getLastName());
        parameters.put("email", license.getUser().getEmail());
        parameters.put("vocabularyname", license.getVocabularyConversion().getName());
        parameters.put("approveUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), TRUE, license.getToken()));
        parameters.put("declineUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), FALSE, license.getToken()));
        return parameters;
    }
}
