package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.vocabulary.VocabularyVersionConverter;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyReleaseVersionServerImplTest {

    public static final int VOCABULARY_VERSION_ID = 19960123;
    public static final int CURRENT_VERSION_ID = 20300101;
    public static final VocabularyReleaseVersionDTO CURREN_VERSION_DTO = new VocabularyReleaseVersionDTO(CURRENT_VERSION_ID, VocabularyVersionConverter.toNewFormat(CURRENT_VERSION_ID), true);

    @Mock
    private VocabularyReleaseVersionRepository vocabularyReleaseVersionRepository;

    @Mock
    private VocabularyServiceV5 vocabularyServiceV5;

    @InjectMocks
    private VocabularyReleaseVersionServerImpl vocabularyReleaseVersionService;

    @Test
    void testIsCurrent() {
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);

        assertTrue(vocabularyReleaseVersionService.isCurrent(CURRENT_VERSION_ID));
        verify(vocabularyServiceV5, times(1)).getReleaseVocabularyVersionId();
    }

    @Test
    void testIsCurrentMissingInHistory() {
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);
        when(vocabularyReleaseVersionRepository.existsById(eq(CURRENT_VERSION_ID))).thenReturn(false);

        assertTrue(vocabularyReleaseVersionService.isCurrentMissingInHistory(CURRENT_VERSION_ID));

    }

    @Test
    void testIsNotCurrent() {
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);

        assertFalse(vocabularyReleaseVersionService.isCurrent(VOCABULARY_VERSION_ID));
        verify(vocabularyServiceV5, times(1)).getReleaseVocabularyVersionId();
    }
    @Test
    void testGetReleaseVersionsWithoutMissing() {

        VocabularyReleaseVersion dummyVersion = createVocabularyReleaseVersion(CURRENT_VERSION_ID);
        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Collections.singletonList(dummyVersion));
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);

        assertEquals(
                Collections.singletonList(toDto(dummyVersion, true)),
                vocabularyReleaseVersionService.getReleaseVersions());

        verify(vocabularyReleaseVersionRepository, times(1)).findAll();
        verify(vocabularyServiceV5, times(1)).getReleaseVocabularyVersionId();
    }

    @Test
    void testGetReleaseVersionsWithMissing() {

        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Collections.emptyList());
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);

        assertEquals(
                Collections.singletonList(CURREN_VERSION_DTO),
                vocabularyReleaseVersionService.getReleaseVersions());

        verify(vocabularyReleaseVersionRepository, times(1)).findAll();
        verify(vocabularyServiceV5, times(1)).getReleaseVocabularyVersionId();
    }


    @Test
    void testGetReleaseVersionsOrder() {
        VocabularyReleaseVersion version1 = createVocabularyReleaseVersion(19960123);
        VocabularyReleaseVersion version3 = createVocabularyReleaseVersion(19960125);
        VocabularyReleaseVersion version2 = createVocabularyReleaseVersion(19960124);

        when(vocabularyReleaseVersionRepository.findAll()).thenReturn(Arrays.asList(version3, version1, version2));
        when(vocabularyServiceV5.getReleaseVocabularyVersionId()).thenReturn(CURRENT_VERSION_ID);

        List<VocabularyReleaseVersionDTO> result = vocabularyReleaseVersionService.getReleaseVersions();

        // Create the expected result in descending order
        List<VocabularyReleaseVersionDTO> expectedResult = Arrays.asList(
                CURREN_VERSION_DTO,
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
                VocabularyVersionConverter.toNewFormat(dummyVersion.getId()),
                current
        );
    }

    private VocabularyReleaseVersion createVocabularyReleaseVersion(int id) {
        VocabularyReleaseVersion dummyVersion = new VocabularyReleaseVersion();
        dummyVersion.setId(id);
        return dummyVersion;
    }
}
