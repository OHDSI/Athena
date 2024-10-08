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

import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.CDMVersion;

import java.util.List;
import java.util.Map;

public interface EmailService {

    void sendVocabularyUpdateNotification(AthenaUser user, List<Notification> updatedVocabularies);

    void sendVocabularyDownloadLink(AthenaUser recipient, String bundleName, String url, CDMVersion version,
                                    Map<String, String> requestedVocabularies, String vocabularyReleaseVersion);

    void sendDeltaDownloadLink(AthenaUser recipient, String bundleName, String url, CDMVersion version, Map<String, String> requestedVocabularies,
                               String vocabularyReleaseVersion, String deltaReleaseVersion);

    void sendLicenseRequestToAdmins(License license);

    void sendFailedSaving(AthenaUser user);

    void sendLicenseAcceptance(AthenaUser user, boolean accepted, String vocabularyName);

    void sendVocabulariesWereSharedNotification(AthenaUser recipient, AthenaUser bundleOwner, String url, CDMVersion cdmVersion,
                                                String vocabularyReleaseVersion);

    void sendDeltaWereSharedNotification(AthenaUser recipient, AthenaUser bundleOwner, String url, CDMVersion cdmVersion,
                                         String vocabularyReleaseVersion, String deltaReleaseVersion);
}
