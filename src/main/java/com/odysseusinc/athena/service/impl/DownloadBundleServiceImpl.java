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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.vocabulary.ReleaseVocabularyVersionConverter;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.SavedFileRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import com.odysseusinc.athena.util.Fn;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.odysseusinc.athena.service.DownloadBundleService.BundleType.*;
import static java.util.stream.Collectors.toList;

@Transactional
@Service
public class DownloadBundleServiceImpl implements DownloadBundleService {

    private final DownloadBundleRepository bundleRepository;
    private final SavedFileRepository fileRepository;
    private final FileHelper fileHelper;

    private final VocabularyServiceV5 vocabularyServiceV5;

    private final VocabularyReleaseVersionService versionService;

    @Autowired
    public DownloadBundleServiceImpl(DownloadBundleRepository bundleRepository, SavedFileRepository fileRepository, FileHelper fileHelper, VocabularyServiceV5 vocabularyServiceV5, VocabularyReleaseVersionService versionService) {

        this.bundleRepository = bundleRepository;
        this.fileRepository = fileRepository;
        this.fileHelper = fileHelper;
        this.vocabularyServiceV5 = vocabularyServiceV5;
        this.versionService = versionService;
    }

    @Override
    public DownloadBundle initBundle(String bundleName, AthenaUser currentUser, CDMVersion version, Integer vocabularyVersion, boolean delta, Integer deltaVersion) {
        return initBundle(bundleName, currentUser.getId(), version, vocabularyVersion, delta, deltaVersion, ReleaseVocabularyVersionConverter.toOldFormat(vocabularyServiceV5.getReleaseVocabularyVersionId()));
    }

    @Override
    public DownloadBundle copyBundle(DownloadBundle bundle, String name) {
        return initBundle(name, bundle.getUserId(), bundle.getCdmVersion(), bundle.getVocabularyVersion(), bundle.isDelta(), bundle.getDeltaVersion(), bundle.getReleaseVersion());
    }

    @Override
    public DownloadBundle get(Long bundleId) {

        return bundleRepository.getOne(bundleId);
    }

    private  DownloadBundle initBundle(String bundleName, Long userId, CDMVersion cdmVersion, Integer vocabularyVersion, boolean delta, Integer deltaVersion, String releaseVersion) {
        return Fn.create(DownloadBundle::new, bundle -> {
            bundle.setUuid(UUID.randomUUID().toString());
            bundle.setCdmVersion(cdmVersion);
            bundle.setCreated(new Date());
            bundle.setUserId(userId);
            bundle.setName(bundleName);
            bundle.setReleaseVersion(releaseVersion);
            bundle.setStatus(DownloadBundleStatus.PENDING);
            bundle.setVocabularyVersion(vocabularyVersion);
            bundle.setDeltaVersion(deltaVersion);
            bundle.setDelta(delta);
        });
    }

    @Override
    public Long getUserId(Long bundleId) {

        return bundleRepository.getUserId(bundleId);
    }

    @Override
    public DownloadBundle save(DownloadBundle bundle) {

        return bundleRepository.save(bundle);
    }

    @Override
    public SavedFile save(SavedFile file) {

        return fileRepository.save(file);
    }

    public void archiveBefore(Date before) {

        Set<DownloadBundle> bundles = bundleRepository.findBefore(before);
        bundles.forEach(uuid -> archiveByUuid(uuid.getUuid()));

        fileRepository.deleteByDownloadBundleIdIn(bundles.stream().map(DownloadBundle::getId).collect(toList()));
    }

    public void archive(Long id) {

        DownloadBundle downloadBundle = get(id);
        if (downloadBundle.isArchived()) {
            return;
        }
        archiveByUuid(downloadBundle.getUuid());
        fileRepository.deleteByDownloadBundleId(downloadBundle.getId());
    }

    @Override
    public void checkBundleOwner(AthenaUser user, long bundleId) {

        DownloadBundle bundle = get(bundleId);
        if (!user.getId().equals(bundle.getUserId())) {
            throw new PermissionDeniedException();
        }
    }

    @Override
    public BundleType getType(DownloadBundle bundle) {
        if (bundle.getCdmVersion() == CDMVersion.V4_5) {
            return V4_5;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && bundle.isDelta()) {
            return V5_DELTAS;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return V5;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && !versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return V5_HISTORIES;
        }
        throw new NotExistException("No savers for version " + bundle.getCdmVersion(), CDMVersion.class);
    }

    @Override
    public void validate(DownloadBundle bundle) {
        validate(bundle, this.getType(bundle));
    }

    @Override
    public void validate(DownloadBundle bundle, BundleType type) {
        switch (type) {
            case V4_5:
                throw new ValidationException("CDM Version 4 is not supported anymore");
            case V5_HISTORIES:
                if (bundle.getVocabularyVersion() == null) {
                    throw new ValidationException("The Vocabulary version should be set.");
                }
                if (!versionService.isPresentInHistory(bundle.getVocabularyVersion())) {
                    throw new ValidationException("Vocabulary version is not found in the history.");
                }
                break;
            case V5_DELTAS:
                if (bundle.getVocabularyVersion() == null) {
                    throw new ValidationException("The Vocabulary version should be set.");
                }
                if (bundle.getDeltaVersion() == null) {
                    throw new ValidationException("The Delta version should be set.");
                }
                if (bundle.getDeltaVersion() >= bundle.getVocabularyVersion()) {
                    throw new ValidationException("The Delta version should be lower than the Vocabulary version");
                }
                if (versionService.isCurrentMissingInHistory(bundle.getVocabularyVersion())){
                    throw new ValidationException("The current version has not been uploaded to historical data. The delta cannot be created. Please contact the administrator.");
                }
                if (!versionService.isPresentInHistory(bundle.getVocabularyVersion())) {
                    throw new ValidationException("Vocabulary version is not found in the history.");
                }
                if (!versionService.isPresentInHistory(bundle.getDeltaVersion())) {
                    throw new ValidationException("Delta version is not found in the history.");
                }
                break;
        }
    }

    private void archiveByUuid(String uuid) {

        FileUtils.deleteQuietly(new File(fileHelper.getZipPath(uuid)));
        bundleRepository.archiveByUuid(uuid);
    }

}
