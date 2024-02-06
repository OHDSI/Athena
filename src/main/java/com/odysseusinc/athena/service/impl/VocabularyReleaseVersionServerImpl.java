package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyReleaseVersionDTO;
import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;
import com.odysseusinc.athena.repositories.v5history.VocabularyReleaseVersionRepository;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Service
@RequiredArgsConstructor
public class VocabularyReleaseVersionServerImpl implements VocabularyReleaseVersionService {

    public static final int MISSING_IN_HISTORY_DUMMY_VERSION_ID = 2100_01_01;

    private final VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    protected final VocabularyServiceV5 vocabularyServiceV5;


    @Override
    public boolean isCurrent(int versionId) {
        if (isCurrentMissingInHistory(versionId)) {
            return true;
        }
        return vocabularyReleaseVersionRepository.findById(versionId).map(this::isCurrent).orElse(false);
    }

    @Override
    public boolean isCurrent(VocabularyReleaseVersion version) {
        return Objects.equals(
                vocabularyServiceV5.getOMOPVocabularyVersion(),
                version.getAthenaName()
        );
    }

    @Override
    public String toReleaseVersion(int versionId) {
        return vocabularyReleaseVersionRepository.findById(versionId).map(VocabularyReleaseVersion::getAthenaName).orElse(String.valueOf(versionId));
    }

    @Override
    public List<VocabularyReleaseVersionDTO> getReleaseVersions() {
        List<VocabularyReleaseVersionDTO> releaseVersions = vocabularyReleaseVersionRepository.findAll().stream()
                .map(v -> new VocabularyReleaseVersionDTO(v.getId(), v.getVocabularyName(), isCurrent(v)))
                .sorted(Comparator.comparing(VocabularyReleaseVersionDTO::getValue))
                .collect(Collectors.toList());
        boolean currentMissingInHistory = releaseVersions.stream().noneMatch(VocabularyReleaseVersionDTO::isCurrent);
        return Stream.concat(
                        currentMissingInHistory ? Stream.of(createCurrentMissingInHistory(vocabularyServiceV5.getOMOPVocabularyVersion())) : Stream.empty(),
                        releaseVersions.stream()
                ).sorted(Comparator.comparing(VocabularyReleaseVersionDTO::getValue).reversed())
                .collect(Collectors.toList());
    }

    // Current version can be missing in the history DB. it means that we cannot create a delta for that version.
    // Before generating a delta, we need to update the historical database with the missing version.
    private VocabularyReleaseVersionDTO createCurrentMissingInHistory(String omopVocabularyVersion) {
        return new VocabularyReleaseVersionDTO(MISSING_IN_HISTORY_DUMMY_VERSION_ID, omopVocabularyVersion, true);
    }

    public boolean isCurrentMissingInHistory(int versionId) {
        return versionId == MISSING_IN_HISTORY_DUMMY_VERSION_ID;
    }
}
