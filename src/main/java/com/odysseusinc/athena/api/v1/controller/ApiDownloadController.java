package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.service.DownloadsHistoryService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.writer.FileHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ApiVocabularyController")
@RestController
@RequestMapping("/api/s2s/vocabularies")
public class ApiDownloadController extends DownloadsController {
    @Autowired
    public ApiDownloadController(DownloadsHistoryService downloadsHistoryService, FileHelper fileHelper, UserService userService, VocabularyService vocabularyService) {
        super(downloadsHistoryService, fileHelper, userService, vocabularyService);
    }
}
