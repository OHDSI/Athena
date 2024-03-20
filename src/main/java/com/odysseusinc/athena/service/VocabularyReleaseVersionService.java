package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyReleaseVersionDTO;

import java.util.List;

public interface VocabularyReleaseVersionService {
    boolean isCurrent(Integer versionId);

    boolean isPresentInHistory(Integer versionId);

    List<VocabularyReleaseVersionDTO> getReleaseVersions();
    boolean isCurrentMissingInHistory(int versionId);
}
