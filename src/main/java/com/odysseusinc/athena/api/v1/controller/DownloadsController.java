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
 * Created: March 24, 2020
 *
 */

package com.odysseusinc.athena.api.v1.controller;


import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.writer.FileHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;

@Api
@RestController
@RequestMapping("/api/v1/vocabularies")
public class DownloadsController {

    private final DownloadsHistoryService downloadsHistoryService;
    private final FileHelper fileHelper;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    public DownloadsController(DownloadsHistoryService downloadsHistoryService, FileHelper fileHelper, UserService userService, VocabularyService vocabularyService) {

        this.downloadsHistoryService = downloadsHistoryService;
        this.fileHelper = fileHelper;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }

    @ApiOperation("Get vocabularies bundle zip.")
    @GetMapping(value = "/zip/{uuid}")
    public void getZippedBundle(
            @PathVariable("uuid") String uuid,
            HttpServletResponse response) throws IOException {

        DownloadBundle bundle = vocabularyService.getDownloadBundle(uuid);

        final Long userId = userService.getCurrentUserId() != null ? userService.getCurrentUserId() : bundle.getUserId();

        vocabularyService.checkBundleVocabularies(bundle.getId(), userId);
        downloadsHistoryService.updateStatistics(bundle, userId);

        String version = bundle.getCdmVersion().name().toLowerCase().replace(".", "_");
        String archiveName = String.format("vocabulary_download_%s_{%s}_%s.zip",
                version, uuid, currentTimeMillis());

        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        response.setContentLengthLong(new File(fileHelper.getZipPath(uuid)).length());
        try (FileInputStream is = new FileInputStream(fileHelper.getZipPath(uuid))) {
            IOUtils.copy(is, response.getOutputStream());
        } finally {
            response.flushBuffer();
        }
    }
}