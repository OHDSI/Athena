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

package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import java.util.List;

public interface VocabularyService {

    List<UserVocabularyDTO> getAllForCurrentUser() throws PermissionDeniedException;

    void saveContent(DownloadBundle bundle, AthenaUser user);

    DownloadBundle saveBundle(String bundleName, List<Integer> idV4s, AthenaUser currentUser, CDMVersion version);

    List<DownloadBundleDTO> getDownloadHistory(Long userId);

    DownloadBundle getDownloadBundle(String uuid);

    DownloadBundle saveDownloadItems(DownloadBundle bundle, List<Integer> idV4s);

    void restoreDownloadBundle(DownloadBundle downloadBundle) throws PermissionDeniedException;

    void checkBundleUser(AthenaUser user, DownloadBundle bundle);

    Iterable<License> saveLicenses(AthenaUser user, List<Integer> vocabularyV4Ids, LicenseStatus status);

    Long requestLicenses(AthenaUser user, Integer vocabularyV4Id);

    void deleteLicense(Long licenseId);

    void acceptLicense(Long id, Boolean accepted);

    License get(AthenaUser user, Integer vocabularyId);

    License get(Long licenseId);

    License get(Long licenseId, String token);

    void notifyAboutUpdates(Long userId, Integer vocabularyId, boolean notify);

    List<Notification> getNotifications(Long userId);

    void checkBundleVocabularies(DownloadBundle bundle, Long userId);
    }
