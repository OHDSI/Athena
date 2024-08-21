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

public enum EmailType {

    FAILED_SAVING("Failed vocabularies saving", "mail/failed_vocabularies_download"),
    FAILED_SENDING_TO_ADMIN("Failed email sending", "mail/failed_email_sending"),
    LICENSE_ACCEPTANCE("Licensing acceptance", "mail/license_acceptance"),
    LICENSE_REQUEST("License request", "mail/license_request"),
    VOCABULARIES_LINK("OHDSI Standardized Vocabularies. Your download link", "mail/vocabularies_download"),
    VOCABULARIES_DELTA_LINK("OHDSI Standardized Vocabularies Delta. Your download link", "mail/vocabularies_delta_download"),
    VOCABULARIES_UPDATE_NOTIFICATION("Vocabulary version update", "mail/vocabularies_update"),
    VOCABULARIES_SHARED_DOWNLOAD("OHDSI Standardized Vocabularies. Your download link", "mail/vocabularies_share_download"),
    VOCABULARIES_DELTA_SHARED_DOWNLOAD("OHDSI Standardized Vocabularies. Your download link", "mail/vocabularies_delta_share_download");

    private final String subject;
    private final String template;

    EmailType(String subject, String template) {

        this.subject = subject;
        this.template = template;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplate() {
        return template;
    }
}