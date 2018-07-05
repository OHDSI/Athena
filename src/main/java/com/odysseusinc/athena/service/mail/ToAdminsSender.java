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
import com.odysseusinc.athena.service.impl.UserService;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

public abstract class ToAdminsSender extends MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToAdminsSender.class);

    private UserService userService;

    public ToAdminsSender(JavaMailSender mailSender, MailContentBuilder contentBuilder, UserService userService) {

        super(mailSender, contentBuilder);
        this.userService = userService;
    }

    public void sendToAdmins(Object object) {

        try {
            List<String> adminMails = userService.getAdmins().stream()
                    .map(AthenaUser::getEmail)
                    .collect(Collectors.toList());

            super.send(adminMails, getParameters(object));
            LOGGER.info("Email for request license is sent to admins with ids: [{}]",
                    StringUtils.join(adminMails, ", "));

        } catch (MessagingException | MailException | UnsupportedEncodingException ignored) {
            // no operation
        }
    }

    protected abstract Map<String, Object> getParameters(Object object);

}
