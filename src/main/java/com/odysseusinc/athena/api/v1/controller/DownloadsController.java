package com.odysseusinc.athena.api.v1.controller;


import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.writer.FileHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.security.access.annotation.Secured;
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
@RequestMapping(value = "/api/v1/vocabularies")
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
    @Secured("ROLE_USER")
    @GetMapping(value = "/zipped/{uuid}")
    public void getZippedBundle(
            @PathVariable("uuid") String uuid,
            HttpServletResponse response) throws IOException {

        DownloadBundle bundle = vocabularyService.getDownloadBundle(uuid);
        final AthenaUser currentUser = userService.getCurrentUser();

        vocabularyService.checkBundleVocabularies(bundle, currentUser.getId());
        downloadsHistoryService.updateStatistics(bundle, currentUser);

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

    @Deprecated
    @ApiOperation("Get zip. Deprecated due to security reasons")
    @GetMapping(value = "/zip/{uuid}")
    public void getAllFiles(
            @PathVariable("uuid") String uuid,
            HttpServletResponse response) throws IOException {

        response.sendRedirect("/vocabulary/download-bundle/" + uuid);
    }

}
