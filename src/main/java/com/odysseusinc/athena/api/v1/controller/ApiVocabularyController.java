package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyVersionDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Tag(name = "ApiVocabularyController")
@RestController
@RequestMapping("/api/s2s/vocabularies")
public class ApiVocabularyController extends AbstractVocabularyController {

    @Override
    public List<DownloadBundleDTO> getDownloadHistory(Principal principal) throws PermissionDeniedException {
        List<DownloadBundleDTO> dtos = super.getDownloadHistory(principal);
        dtos.forEach(ApiVocabularyController::setLink);
        return dtos;
    }

    @Override
    public DownloadBundleDTO createAndGenerate(@RequestParam(value = "ids") List<Integer> idV4s,
                                                                        @RequestParam(value = "name") String bundleName,
                                                                        @RequestParam(value = "vocabularyVersion", required = false) Integer vocabularyVersion,
                                                                        @RequestParam(value = "delta", defaultValue = "false") boolean delta,
                                                                        @RequestParam(value = "deltaVersion", required = false) Integer deltaVersion) throws IOException {
        DownloadBundleDTO dto = super.createAndGenerate(idV4s, bundleName, vocabularyVersion, delta, deltaVersion);
        setLink(dto);
        return dto;
    }

    @Override
    public DownloadBundleDTO copyAndGenerate(@RequestParam("id") Long bundleId, @RequestParam("name") String bundleName) throws PermissionDeniedException {
        DownloadBundleDTO dto = super.copyAndGenerate(bundleId, bundleName);
        setLink(dto);
        return dto;
    }

    @Override
    public VocabularyVersionDTO vocabularyReleaseVersion() {
        return super.vocabularyReleaseVersion();
    }

    @Override
    public Integer vocabularyReleaseVersionCode() {
        return super.vocabularyReleaseVersionCode();
    }

    private static void setLink(DownloadBundleDTO dto) {
        dto.setLink(Optional.ofNullable(dto.getLink()).map(link -> link.replaceAll("/api/v1/", "/api/s2s/")).orElse(null));
    }

}
