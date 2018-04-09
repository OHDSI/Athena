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

package com.odysseusinc.athena.service.impl;

import static java.util.stream.Collectors.toList;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.SavedFileRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.writer.FileHelper;
import java.io.File;
import java.util.Date;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadBundleServiceImpl implements DownloadBundleService {
    @Autowired
    private DownloadBundleRepository bundleRepository;
    @Autowired
    private SavedFileRepository fileRepository;
    @Autowired
    private FileHelper fileHelper;

    @Override
    public DownloadBundle get(Long bundleId) {

        return bundleRepository.findOne(bundleId);
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
        bundles.forEach((uuid) -> archiveByUuid(uuid.getUuid()));

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

    private void archiveByUuid(String uuid) {

        new File(fileHelper.getZipPath(uuid)).delete();
        bundleRepository.archiveByUuid(uuid);
    }

}
