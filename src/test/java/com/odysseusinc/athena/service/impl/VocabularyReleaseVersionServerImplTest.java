package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyReleaseVersionDTO;
import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;
import com.odysseusinc.athena.repositories.v5history.VocabularyReleaseVersionRepository;
import com.odysseusinc.athena.service.VocabularyServiceV5;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.odysseusinc.athena.service.impl.VocabularyReleaseVersionServerImpl.MISSING_IN_HISTORY_DUMMY_VERSION_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyReleaseVersionServerImplTest {

    public static final String ATHENA_NAME = "Write Once, Run Anywhere";
    public static final String VOCABULARY_NAME = "Java 1.0";
    public static final String MISSING_VOCABULARY_NAME = "Java 1_000_0000";
    public static final int VOCABULARY_VERSION_ID = 19960123;

    @Mock
    private VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    @Mock
    private VocabularyServiceV5 vocabularyServiceV5;

    @InjectMocks
    private VocabularyReleaseVersionServerImpl vocabularyReleaseVersionService;

    @Test
    void testIsCurrent() {
        when(vocabularyReleaseVersionRepository.findById(VOCABULARY_VERSION_ID)).thenReturn(Optional.of(createVocabularyReleaseVersion(VOCABULARY_VERSION_ID, VOCABULARY_NAME, ATHENA_NAME)));
        when(vocabularyServiceV5.getOMOPVocabularyVersion()).thenReturn(ATHENA_NAME);

        assertTrue(vocabularyReleaseVersionService.isCurrent(VOCABULARY_VERSION_ID));
        verify(vocabularyReleaseVersionRepository, times(1)).findById(VOCABULARY_VERSION_ID);
    }

    @Test
    void testIsCurrentMissingInHistory() {
        assertTrue(vocabularyReleaseVersionService.isCurrentMissingInHistory(MISSING_IN_HISTORY_DUMMY_VERSION_ID));
    }

    @Test
    void testIsNotCurrent() {
        int missingVocabularyVersionId = 20500101;
        when(vocabularyReleaseVersionRepository.findById(missingVocabularyVersionId)).thenReturn(Optional.empty());

        assertFalse(vocabularyReleaseVersionService.isCurrent(missingVocabularyVersionId));
        verify(vocabularyReleaseVersionRepository, times(1)).findById(missingVocabularyVersionId);
    }
    @Test
    void testGetReleaseVersionsWithoutMissing() {

        VocabularyReleaseVersion dummyVersion = createVocabularyReleaseVersion(19960123, VOCABULARY_NAME, ATHENA_NAME);
        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Collections.singletonList(dummyVersion));
        when(vocabularyServiceV5.getOMOPVocabularyVersion()).thenReturn(ATHENA_NAME);

        assertEquals(
                Collections.singletonList(toDto(dummyVersion, true)),
                vocabularyReleaseVersionService.getReleaseVersions());

        verify(vocabularyReleaseVersionRepository, times(1)).findAll();
        verify(vocabularyServiceV5, times(1)).getOMOPVocabularyVersion();
    }

    @Test
    void testGetReleaseVersionsWithMissing() {

        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Collections.emptyList());
        when(vocabularyServiceV5.getOMOPVocabularyVersion()).thenReturn(MISSING_VOCABULARY_NAME);

        assertEquals(
                Collections.singletonList(new VocabularyReleaseVersionDTO(MISSING_IN_HISTORY_DUMMY_VERSION_ID, MISSING_VOCABULARY_NAME, true)),
                vocabularyReleaseVersionService.getReleaseVersions());

        verify(vocabularyReleaseVersionRepository, times(1)).findAll();
        verify(vocabularyServiceV5, times(1)).getOMOPVocabularyVersion();
    }


    @Test
    void testGetReleaseVersionsOrder() {
        VocabularyReleaseVersion version1 = createVocabularyReleaseVersion(19960123, "Vocabulary1", ATHENA_NAME + "1");
        VocabularyReleaseVersion version3 = createVocabularyReleaseVersion(19960125, "Vocabulary3", ATHENA_NAME + "1");
        VocabularyReleaseVersion version2 = createVocabularyReleaseVersion(19960124, "Vocabulary2", ATHENA_NAME + "1");

        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Arrays.asList(version3, version1, version2));
        when(vocabularyServiceV5.getOMOPVocabularyVersion()).thenReturn(ATHENA_NAME);

        List<VocabularyReleaseVersionDTO> result = vocabularyReleaseVersionService.getReleaseVersions();

        // Create the expected result in descending order
        VocabularyReleaseVersionDTO currentVersion = new VocabularyReleaseVersionDTO(MISSING_IN_HISTORY_DUMMY_VERSION_ID, ATHENA_NAME, true);
        List<VocabularyReleaseVersionDTO> expectedResult = Arrays.asList(
                currentVersion,
                toDto(version3, false),
                toDto(version2, false),
                toDto(version1, false)
        );

        assertIterableEquals(expectedResult, result);

        verify(vocabularyReleaseVersionRepository, times(1)).findAll();
    }


    private VocabularyReleaseVersionDTO toDto(VocabularyReleaseVersion dummyVersion, boolean current) {
        return new VocabularyReleaseVersionDTO(
                dummyVersion.getId(),
                dummyVersion.getVocabularyName(),
                current
        );
    }

    private VocabularyReleaseVersion createVocabularyReleaseVersion(int id, String vocabularyName, String athenaName) {
        VocabularyReleaseVersion dummyVersion = new VocabularyReleaseVersion();
        dummyVersion.setId(id);
        dummyVersion.setVocabularyName(vocabularyName);
        dummyVersion.setAthenaName(athenaName);
        return dummyVersion;
    }
}
