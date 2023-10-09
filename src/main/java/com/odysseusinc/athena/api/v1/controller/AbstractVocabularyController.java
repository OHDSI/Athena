package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.LicenseService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import com.odysseusinc.athena.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

public class AbstractVocabularyController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(VocabularyController.class);
    @Autowired
    protected  ConverterUtils converterUtils;
    @Autowired
    protected  DownloadBundleService downloadBundleService;
    @Autowired
    protected  DownloadShareService downloadShareService;
    @Autowired
    protected  UserService userService;
    @Autowired
    protected  VocabularyConversionService vocabularyConversionService;
    @Autowired
    protected  VocabularyService vocabularyService;
    @Autowired
    protected  LicenseService licenseService;
    @Autowired
    protected  VocabularyServiceV5 vocabularyServiceV5;

    @Operation(summary = "Get vocabularies.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserVocabularyDTO>> getAllForCurrentUser() {
        return ResponseEntity.ok(vocabularyService.getAllForCurrentUser());
    }

    @Operation(summary = "Get download history.")
    @GetMapping("/downloads")
    public List<DownloadBundleDTO> getDownloadHistory(Principal principal) throws PermissionDeniedException {
        AthenaUser user = userService.getUser(principal);
        return vocabularyService.getDownloadHistory(user);
    }
}
