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

package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.RestoreAvailabilityDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.CDMVersion;
import java.util.List;
import java.util.Optional;

public interface VocabularyService {

    List<UserVocabularyDTO> getAllForCurrentUser();

    void generateBundle(DownloadBundle bundle, AthenaUser user);

    DownloadBundle saveBundle(String bundleName, List<Integer> idV4s, AthenaUser currentUser, CDMVersion version, Integer vocabularyVersion, boolean delta, Integer deltaVersion);

    DownloadBundle copyBundle(Long bundleId, String name, AthenaUser currentUser);

    List<DownloadBundleDTO> getDownloadHistory(AthenaUser user);

    DownloadBundle getDownloadBundle(String uuid);

    DownloadBundle saveDownloadItems(DownloadBundle bundle, List<Integer> idV4s);

    void restoreDownloadBundle(long bundleId);

    RestoreAvailabilityDTO getRestoreAvailability(long bundleId);

    DownloadBundle regenerateDownloadBundleFromCurrentVersion(long bundleId);

    void checkBundleUser(AthenaUser user, DownloadBundle bundle);

    void checkBundleAndSharedUser(AthenaUser user, DownloadBundle bundle);

    /**
     * Same check, for callers that only have the id. Loads the bundle so that an endpoint
     * taking a bundle id from the request cannot act on a bundle the caller has no claim to.
     */
    void checkBundleAndSharedUser(AthenaUser user, long bundleId);

    Iterable<License> grantLicenses(AthenaUser user, List<Integer> vocabularyV4Ids, AthenaUser grantedBy);

    Long requestLicense(AthenaUser user, Integer vocabularyV4Id);

    void deleteLicense(Long licenseId);

    void cancelPendingLicense(Long licenseId);

    void acceptLicense(Long id, boolean accepted, AthenaUser resolvedBy);

    long countPendingLicenses();

    License get(AthenaUser user, Integer vocabularyId);

    Optional<License> get(Long licenseId);

    License get(Long licenseId, String token);

    List<Notification> getNotifications(Long userId);

    void checkBundleVocabularies(long bundleId, Long userId);
    }
