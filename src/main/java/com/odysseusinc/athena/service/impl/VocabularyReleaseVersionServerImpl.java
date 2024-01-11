package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;
import com.odysseusinc.athena.repositories.v5history.VocabularyReleaseVersionRepository;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class VocabularyReleaseVersionServerImpl implements VocabularyReleaseVersionService {

    private final VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    protected final VocabularyServiceV5 vocabularyServiceV5;

    public List<VocabularyReleaseVersion> getAll() {
        return vocabularyReleaseVersionRepository.findAll();
    }

    public Optional<VocabularyReleaseVersion> get(int id) {
        return vocabularyReleaseVersionRepository.findById(id);
    }

    public boolean isCurrent(int id) {
        return get(id).map(this::isCurrent).orElse(false);
    }

    public boolean isCurrent(VocabularyReleaseVersion version) {
        return Objects.equals(
                vocabularyServiceV5.getOMOPVocabularyVersion(),
                version.getAthenaName()
        );
    }

}
