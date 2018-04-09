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

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import java.util.Date;

public interface DownloadBundleService {

    DownloadBundle get(Long bundleId);

    Long getUserId(Long bundleId);

    DownloadBundle save(DownloadBundle bundle);

    SavedFile save(SavedFile file);

    void archiveBefore(Date before);

    void archive(Long id);
}
