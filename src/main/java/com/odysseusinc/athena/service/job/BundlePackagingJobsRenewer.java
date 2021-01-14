/*
 *
 * Copyright 2021 Odysseus Data Services, inc.
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
 * Authors: Alex Cumarav
 * Created: January 14, 2021
 *
 */
package com.odysseusinc.athena.service.job;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.service.impl.AsyncVocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.odysseusinc.athena.util.DownloadBundleStatus.PENDING;

@Component
public class BundlePackagingJobsRenewer {

    private final AsyncVocabularyService asyncVocabularyService;
    private final DownloadBundleRepository downloadBundleRepository;
    private final UserService userService;

    public BundlePackagingJobsRenewer(AsyncVocabularyService asyncVocabularyService, DownloadBundleRepository downloadBundleRepository, UserService userService) {

        this.asyncVocabularyService = asyncVocabularyService;
        this.downloadBundleRepository = downloadBundleRepository;
        this.userService = userService;
    }

    @EventListener(classes = ContextRefreshedEvent.class)
    @Transactional
    public void resumeNotCompletedJobs() {

        for (DownloadBundle uncompletedBundle : downloadBundleRepository.findByStatus(PENDING)) {

            AthenaUser bundleOwner = userService.get(uncompletedBundle.getUserId());
            asyncVocabularyService.saveContent(uncompletedBundle, bundleOwner);
        }
    }
}