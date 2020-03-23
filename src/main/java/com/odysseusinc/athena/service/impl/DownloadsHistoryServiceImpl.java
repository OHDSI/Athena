/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Created: March 20, 2020
 *
 */

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadHistory;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadHistoryRepository;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
@Service
public class DownloadsHistoryServiceImpl implements DownloadsHistoryService {

    private final DownloadHistoryRepository downloadHistoryRepository;
    private final UserService userService;

    public DownloadsHistoryServiceImpl(DownloadHistoryRepository downloadHistoryRepository, UserService userService) {

        this.downloadHistoryRepository = downloadHistoryRepository;
        this.userService = userService;
    }

    @Override
    public void updateStatistics(DownloadBundle bundle, AthenaUser currentUser) {

        DownloadHistory downloadRecord = new DownloadHistory();
        downloadRecord.setUserId(currentUser.getId());
        downloadRecord.setVocabularyBundle(bundle);
        downloadRecord.setDownloadTime(new Date());

        downloadHistoryRepository.save(downloadRecord);
    }
}
