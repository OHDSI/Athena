package com.odysseusinc.athena.service;

import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;

import java.util.List;

public interface VocabularyReleaseVersionService {
    List<VocabularyReleaseVersion> getAll();
    boolean isCurrent(int id);
    boolean isCurrent(VocabularyReleaseVersion version);
}
