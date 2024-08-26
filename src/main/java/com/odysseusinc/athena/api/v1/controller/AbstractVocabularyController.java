package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyVersionDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.LicenseService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static com.odysseusinc.athena.util.CDMVersion.V5;

public class AbstractVocabularyController {
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
    protected VocabularyReleaseVersionService vocabularyReleaseVersionService;
    @Autowired
    protected GenericConversionService conversionService;

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

    @Operation(summary = "Restore download history item.")
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable("id") Long bundleId)
            throws PermissionDeniedException {

        vocabularyService.restoreDownloadBundle(bundleId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Save and generate vocabularies.")
    @GetMapping("/save")
    public DownloadBundleDTO createAndGenerate(
            // TODO The vocabulary ID is obsolete for CDM4; it should not be used anymore. Instead, we should use codes (the new string ID in CDM5).
            @RequestParam(value = "ids") List<Integer> idV4s,
            @RequestParam(value = "name", required = false) String bundleName,
            @RequestParam(value = "vocabularyVersion", required = false) Integer vocabularyVersion,
            @RequestParam(value = "delta", defaultValue = "false") boolean delta,
            @RequestParam(value = "deltaVersion", required = false) Integer deltaVersion) throws IOException {

        vocabularyVersion = vocabularyVersion != null ? vocabularyVersion :
                vocabularyReleaseVersionService.getCurrent();

        AthenaUser user = userService.getCurrentUser();
        DownloadBundle bundle = vocabularyService.saveBundle(
                bundleName, idV4s, user, V5,
                vocabularyVersion, delta, deltaVersion
        );
        vocabularyService.generateBundle(bundle, user);
        return conversionService.convert(bundle, DownloadBundleDTO.class);
    }

    @Operation(summary = "Restore download history item.")
    @PostMapping("/copy-and-generate")
    public DownloadBundleDTO copyAndUpdate(@RequestParam("id") Long bundleId, @RequestParam("name") String bundleName) throws PermissionDeniedException {
        AthenaUser user = userService.getCurrentUser();
        DownloadBundle bundle = vocabularyService.copyBundle(bundleId, bundleName, user);
        vocabularyService.generateBundle(bundle, user);
        return conversionService.convert(bundle, DownloadBundleDTO.class);
    }

    @GetMapping("/release-version")
    public VocabularyVersionDTO vocabularyReleaseVersion() {
        return converterUtils.convert(vocabularyReleaseVersionService.getCurrentFormatted(), VocabularyVersionDTO.class);
    }
    //The endpoint path should be unified release_version/vocabulary_release_version_code
    @GetMapping("/vocabulary-release-version-code")
    public Integer vocabularyReleaseVersionCode() {
        return vocabularyReleaseVersionService.getCurrent();
    }

}
