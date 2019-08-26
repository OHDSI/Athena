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

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.CDMVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Map;

@Service
public class VocabulariesShareSender extends VocabulariesSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabulariesShareSender.class);

    @Autowired
    public VocabulariesShareSender(JavaMailSender mailSender, MailContentBuilder contentBuilder,
                                   FailedSendingToAdminSender failedSendingToAdminSender) {

        super(mailSender, contentBuilder, failedSendingToAdminSender);
    }

    public void send(AthenaUser user, AthenaUser owner, String url, CDMVersion version) throws MessagingException, MailException {

        try {
            super.send(user, getParameters(user, owner, url, version));
            LOGGER.info("Email with link for download zip is sent to user with id: [{}], zip link: [{}]",
                    user.getId(), url);

        } catch (Exception ex) {
            failedSendingToAdminSender.send(ex);
        }
    }

    @Override
    public String getTemplateName() {

        return "mail/vocabularies_share_download";
    }

    protected Map<String, Object> getParameters(AthenaUser user, AthenaUser owner, String url, CDMVersion version) {

        Map<String, Object> parameters = super.getParameters(url, version);
        parameters.put("name", user.getUsername());
        parameters.put("owner_name", owner.getUsername());
        return parameters;
    }
}
