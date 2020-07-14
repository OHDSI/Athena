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
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Transactional(readOnly = true)
@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailSenderService emailSenderService;
    private final MailContentBuilder contentBuilder;
    private final UrlBuilder urlBuilder;
    private final UserService userService;

    @Value("${vocabularies.download.control.files.url}")
    private String controlFilesUrl;
    @Value("${vocabularies.download.forum.url}")
    private String forumUrl;
    @Value("${vocabularies.download.umls.url}")
    private String umlsUrl;

    public EmailServiceImpl(EmailSenderService emailSenderService, MailContentBuilder contentBuilder, UrlBuilder urlBuilder, UserService userService) {

        this.emailSenderService = emailSenderService;
        this.contentBuilder = contentBuilder;
        this.urlBuilder = urlBuilder;
        this.userService = userService;
    }

    @Override
    public void sendVocabularyUpdateNotification(AthenaUser user, List<Notification> updatedVocabularies) {

        final Map<String, String> vocabularyDetails = new HashMap<>();
        updatedVocabularies.forEach(v -> vocabularyDetails.put(v.getVocabularyCode(), v.getVocabularyConversion().getName()));
        final EmailRecipients recipients = EmailRecipients.builder().to(asList(user.getEmail())).build();
        send(EmailType.VOCABULARIES_UPDATE_NOTIFICATION, ImmutableMap.of("UPDATED_VOCABULARIES", vocabularyDetails), recipients, emptyList());
    }


    @Override
    public void sendVocabularyDownloadLink(AthenaUser user, String url, CDMVersion version, String vocabularyReleaseVersion, String bundleName, Map<String, String> requestedVocabularies) {

        final EmailRecipients recipients = EmailRecipients.builder().to(asList(user.getEmail())).build();
        send(EmailType.VOCABULARIES_LINK, buildParameters(url, version, vocabularyReleaseVersion, bundleName, requestedVocabularies), recipients, getAdminEmails());
        log.info("Email with link for download zip is sent to user with id: [{}], zip link: [{}]", user.getId(), url);
    }

    @Override
    public void sendFailedSaving(AthenaUser user) {

        final EmailRecipients recipients = EmailRecipients.builder().to(asList(user.getEmail())).build();
        send(EmailType.FAILED_SAVING, Collections.emptyMap(), recipients, emptyList());
    }

    @Override
    public void sendLicenseRequestToAdmins(License license) {

        final Map<String, Object> licenceRequestEmailParameters = getParameters(license);
        final EmailRecipients recipients = EmailRecipients.builder()
                .to(getAdminEmails())
                .replyTo(license.getUser().getEmail()).build();
        send(EmailType.LICENSE_REQUEST, licenceRequestEmailParameters, recipients, getAdminEmails());
    }

    @Override
    public void sendLicenseAcceptance(AthenaUser user, boolean accepted, String vocabularyName) {

        final EmailRecipients recipients = EmailRecipients.builder().to(asList(user.getEmail())).build();
        send(EmailType.LICENSE_ACCEPTANCE, getParameters(accepted, vocabularyName), recipients, getAdminEmails());
        log.info("Notification with acceptance solution [{}] is sent to user with id: [{}]", accepted, user.getId());
    }

    @Override
    public void sendVocabulariesWereSharedNotification(AthenaUser recipient, AthenaUser bundleOwner, DownloadBundle bundle) {

        final String bundleUrl = urlBuilder.downloadVocabulariesLink(bundle.getUuid());
        final Map<String, Object> emailParameters = getParameters(recipient, bundleOwner, bundleUrl, bundle.getCdmVersion(), bundle.getReleaseVersion());

        final EmailRecipients recipients = EmailRecipients.builder().to(asList(recipient.getEmail())).build();
        send(EmailType.VOCABULARIES_SHARED_DOWNLOAD, emailParameters, recipients, getAdminEmails());
        log.info("Email with link for download zip is sent to user with id: [{}], zip link: [{}]", recipient.getId(), bundleUrl);
    }

    private void send(EmailType messageType, Map<String, Object> parameters, EmailRecipients recipients, List<String> notifyOnFailureEmails) {

        log.debug("Sending {} to {}", messageType, recipients);
        final String emailBody = contentBuilder.build(messageType.getTemplate(), parameters);

        emailSenderService.sendAsync(messageType.getSubject(), emailBody, recipients)
                .exceptionally(ex -> handleError(ex, messageType, emailBody, recipients.toString(), notifyOnFailureEmails));
    }

    private Void handleError(Throwable ex, EmailType messageType, String emailBody, String recipients, List<String> notifyOnFailureEmails) {

        log.error("Failed email {} \n\n\n{}\n\n\n, to: {}", messageType, emailBody, recipients, ex);
        if (CollectionUtils.isNotEmpty(notifyOnFailureEmails)) {
            final Map<String, Object> parameters = Collections.singletonMap("exception", ex.getCause());

            final EmailRecipients supportEmails = EmailRecipients.builder().to(notifyOnFailureEmails).build();
            send(EmailType.FAILED_SENDING_TO_ADMIN, parameters, supportEmails, emptyList());
        }
        return null;
    }

    private List<String> getAdminEmails() {

        return userService.getAdmins().stream()
                .map(AthenaUser::getEmail).collect(Collectors.toList());
    }

    private Map<String, Object> buildParameters(String url, CDMVersion version, String vocabularyReleaseVersion, String bundleName, Map<String, String> requestedVocabularies) {

        final Map<String, Object> parameters = buildParameters(url, version, vocabularyReleaseVersion);
        parameters.put("vocabularies", requestedVocabularies);
        parameters.put("bundleName", bundleName);
        return parameters;
    }

    private Map<String, Object> buildParameters(String url, CDMVersion version, String vocabularyReleaseVersion) {

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("forumUrl", forumUrl);
        parameters.put("controlFilesUrl", controlFilesUrl);
        parameters.put("url", url);
        parameters.put("umlsUrl", umlsUrl);
        parameters.put("version", String.valueOf((int) version.getValue()));
        parameters.put("vocabularyReleaseVersion", vocabularyReleaseVersion);
        return parameters;
    }

    private Map<String, Object> getParameters(@NotNull Boolean accepted, String vocabularyName) {

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("downloadVocabulariesPageUrl", urlBuilder.downloadVocabulariesPageUrl());
        parameters.put("accepted", accepted);
        parameters.put("vocabularyName", vocabularyName);
        return parameters;
    }

    private Map<String, Object> getParameters(License license) {

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", license.getUser().getFirstName() + ' ' + license.getUser().getLastName());
        parameters.put("email", license.getUser().getEmail());
        parameters.put("organization", license.getUser().getOrganization());
        parameters.put("vocabularyname", license.getVocabularyConversion().getName());
        parameters.put("approveUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), TRUE, license.getToken()));
        parameters.put("declineUrl", urlBuilder.acceptLicenseRequestLink(license.getId(), FALSE, license.getToken()));
        return parameters;
    }

    protected Map<String, Object> getParameters(AthenaUser recipient, AthenaUser owner, String url, CDMVersion version,
                                                String vocabularyReleaseVersion) {

        final Map<String, Object> parameters = buildParameters(url, version, vocabularyReleaseVersion);
        parameters.put("name", recipient.getUsername());
        parameters.put("owner_name", owner.getUsername());
        return parameters;
    }
}
