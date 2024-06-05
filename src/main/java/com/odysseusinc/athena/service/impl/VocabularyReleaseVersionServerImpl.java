package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.vocabulary.ReleaseVocabularyVersionConverter;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyReleaseVersionDTO;
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

    private final VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    protected final VocabularyServiceV5 vocabularyServiceV5;

    @Override
    public boolean isCurrent(Integer versionId) {
        if (versionId == null) {
            return false;
        }
        return Objects.equals(
                vocabularyServiceV5.getReleaseVocabularyVersionId(),
                versionId
        );
    }

    @Override
    public Integer getCurrent() {
        return vocabularyServiceV5.getReleaseVocabularyVersionId();
    }

    @Override
    public String getCurrentFormatted() {
        return ReleaseVocabularyVersionConverter.toNewFormat(getCurrent());
    }

    @Override
    public boolean isPresentInHistory(Integer versionId) {
        return vocabularyReleaseVersionRepository.existsById(versionId);
    }

    @Override
    public List<VocabularyReleaseVersionDTO> getReleaseVersions() {
        List<VocabularyReleaseVersionDTO> releaseVersions = vocabularyReleaseVersionRepository.findAll().stream()
                .map(v -> new VocabularyReleaseVersionDTO(
                        v.getId(),
                        ReleaseVocabularyVersionConverter.toNewFormat(v.getId()),
                        isCurrent(v.getId())))
                .sorted(Comparator.comparing(VocabularyReleaseVersionDTO::getValue))
                .collect(Collectors.toList());
        boolean currentMissingInHistory = releaseVersions.stream().noneMatch(VocabularyReleaseVersionDTO::isCurrent);
        return Stream.concat(
                        currentMissingInHistory ? Stream.of(createCurrentMissingInHistory(vocabularyServiceV5.getReleaseVocabularyVersionId())) : Stream.empty(),
                        releaseVersions.stream()
                ).sorted(Comparator.comparing(VocabularyReleaseVersionDTO::getValue).reversed())
                .collect(Collectors.toList());
    }

    // Current version can be missing in the history DB. it means that we cannot create a delta for that version.
    // Before generating a delta, we need to update the historical database with the missing version.
    private VocabularyReleaseVersionDTO createCurrentMissingInHistory(Integer versionId) {
        return new VocabularyReleaseVersionDTO(versionId, ReleaseVocabularyVersionConverter.toNewFormat(versionId), true);
    }

    public boolean isCurrentMissingInHistory(int versionId) {

        if (!isCurrent(versionId)) {
            return false;
        }
        return !isPresentInHistory(versionId);
    }
}
