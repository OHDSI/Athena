package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        dtos.forEach(dto -> dto.setLink(Optional.ofNullable(dto.getLink()).map(link -> link.replaceAll("/api/v1/", "/api/s2s/")).orElse(null)));
        return dtos;
    }
}
