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

import com.odysseusinc.athena.service.impl.UserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FailedSendingToAdminSender extends ToAdminsSender {

    public FailedSendingToAdminSender(JavaMailSender mailSender, MailContentBuilder contentBuilder, UserService userService) {

        super(mailSender, contentBuilder, userService);
    }

    public void send(Exception exception) {

        super.sendToAdmins(exception);
    }

    @Override
    public String getSubject() {

        return "Failed email sending";
    }

    @Override
    public String getTemplateName() {

        return "mail/failed_email_sending";
    }

    protected Map<String, Object> getParameters(Object exception) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("exception", exception.toString());
        return parameters;
    }

}
