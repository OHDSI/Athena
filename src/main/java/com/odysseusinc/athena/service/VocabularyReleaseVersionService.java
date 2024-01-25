package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyReleaseVersionDTO;
import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;

import java.util.List;

public interface VocabularyReleaseVersionService {
    boolean isCurrent(int versionId);
    boolean isCurrent(VocabularyReleaseVersion version);
    List<VocabularyReleaseVersionDTO> getReleaseVersions();
    boolean isCurrentMissingInHistory(int versionId);
}
