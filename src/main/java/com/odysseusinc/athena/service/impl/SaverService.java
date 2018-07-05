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

import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.saver.ISaver;
import com.odysseusinc.athena.service.writer.FileHelper;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class SaverService {

    private DownloadBundleService bundleService;
    private List ids;
    private FileHelper fileHelper;

    public SaverService(DownloadBundleService bundleService, List ids, FileHelper fileHelper) {

        this.bundleService = bundleService;
        this.ids = ids;
        this.fileHelper = fileHelper;
    }

    public DownloadBundle save(ZipOutputStream zos, DownloadBundle bundle, List<? extends ISaver> savers)
            throws PermissionDeniedException {

        bundle.setCpt4(savers.stream().anyMatch(saver -> saver.containCpt4(ids)));
        DownloadBundle result = bundleService.save(bundle);

        savers.forEach(saver -> saver.save(zos, result, ids));
        fileHelper.deleteTempDirectory(bundle.getUuid());
        return result;
    }

}
