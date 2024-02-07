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

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.SavedFileRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Date;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Transactional
@Service
public class DownloadBundleServiceImpl implements DownloadBundleService {

    private final DownloadBundleRepository bundleRepository;
    private final SavedFileRepository fileRepository;
    private final FileHelper fileHelper;

    private final VocabularyReleaseVersionService versionService;

    @Autowired
    public DownloadBundleServiceImpl(DownloadBundleRepository bundleRepository, SavedFileRepository fileRepository, FileHelper fileHelper, VocabularyReleaseVersionService versionService) {

        this.bundleRepository = bundleRepository;
        this.fileRepository = fileRepository;
        this.fileHelper = fileHelper;
        this.versionService = versionService;
    }

    @Override
    public DownloadBundle get(Long bundleId) {

        return bundleRepository.getOne(bundleId);
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
            return BundleType.V4_5;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && bundle.isDelta()) {
            return BundleType.V5_DELTAS;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return BundleType.V5;
        } else if (bundle.getCdmVersion() == CDMVersion.V5 && !versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return BundleType.V5_HISTORIES;
        }
        throw new NotExistException("No savers for version " + bundle.getCdmVersion(), CDMVersion.class);
    }

    private void archiveByUuid(String uuid) {

        FileUtils.deleteQuietly(new File(fileHelper.getZipPath(uuid)));
        bundleRepository.archiveByUuid(uuid);
    }

}
