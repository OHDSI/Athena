package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VocabularyControllerValidationTest {

    @InjectMocks
    private VocabularyController vocabularyController;

    @Mock
    private VocabularyReleaseVersionService versionService;

    @Test
    void testSaveWithInvalidCmdVersion() {
        float invalidCmdVersion = 10.0f;
        ValidationException exception = assertThrows(ValidationException.class, () ->
                vocabularyController.save(invalidCmdVersion, Collections.emptyList(), "TestBundle", null, false, null));
        assertEquals("No supported CDM version " + invalidCmdVersion, exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndCurrentVersionMissingInHistory() {
        float validCmdVersion = 5.0f;
        int vocabularyVersion = 20220115;
        boolean delta = true;

        when(versionService.isCurrentMissingInHistory(vocabularyVersion)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                vocabularyController.save(validCmdVersion, Collections.emptyList(), "TestBundle", vocabularyVersion, delta, null));
        assertEquals("The current version has not been uploaded to historical data. The delta cannot be created. Please contact the administrator.", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndMissingDeltaVersion() {
        float validCmdVersion = 5.0f;
        int vocabularyVersion = 20220115; 
        boolean delta = true;

        when(versionService.isCurrentMissingInHistory(vocabularyVersion)).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                vocabularyController.save(validCmdVersion, Collections.emptyList(), "TestBundle", vocabularyVersion, delta, null));
        assertEquals("The Delta version should be set.", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndInvalidDeltaVersion() {
        float validCmdVersion = 5.0f;
        int vocabularyVersion = 20220115; 
        boolean delta = true;
        Integer invalidDeltaVersion = 20220116; 

        when(versionService.isCurrentMissingInHistory(vocabularyVersion)).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                vocabularyController.save(validCmdVersion, Collections.emptyList(), "TestBundle", vocabularyVersion, delta, invalidDeltaVersion));
        assertEquals("The Delta version should be lower than the Vocabulary version", exception.getMessage());
    }
}