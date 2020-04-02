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

package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collection;


public interface DownloadsHistoryService {

    void updateStatistics(DownloadBundle bundle, Long userId);

    Collection<DownloadHistoryDTO> retrieveStatistics(LocalDateTime from, LocalDateTime to, Boolean licensedOnly, String[] keywords);

    void generateCSV(Collection<DownloadHistoryDTO> records, OutputStream osw) throws IOException;
}
