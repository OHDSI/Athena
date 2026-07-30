package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.Fn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DownloadBundleServiceImplTest {

    @InjectMocks
    private DownloadBundleServiceImpl bundleService;

    @Mock
    private VocabularyReleaseVersionService versionService;

    @Test
    void testSaveWithUnsupportedCDMVersion() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V4_5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
        });
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("CDM Version 4 is not supported anymore", exception.getMessage());
    }

    @Test
    void testSaveWithNullVocabularyVersion() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
        });
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("The Vocabulary version should be set.", exception.getMessage());
    }

    @Test
    void testSaveWithInvalidVocabularyVersionForV5Histories() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
        });
        when(versionService.isPresentInHistory(downloadBundle.getVocabularyVersion())).thenReturn(false);
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("Vocabulary version is not found in the history.", exception.getMessage());
    }

    @Test
    void testValidateWithVocabularyVersionNotFoundInHistory() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
        });
        when(versionService.isPresentInHistory(downloadBundle.getVocabularyVersion())).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("Vocabulary version is not found in the history.", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndCurrentVersionMissingInHistory() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(2022_01_01);
            bundle.setDelta(true);
        });
        when(versionService.isCurrentMissingInHistory(downloadBundle.getVocabularyVersion())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("The current version has not been uploaded to historical data. The delta cannot be created. Please contact the administrator.", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndMissingDeltaVersion() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDelta(true);
        });
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate( downloadBundle));
        assertEquals("The Delta version should be set.", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaAndInvalidDeltaVersion() {

        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(3024_01_01);
            bundle.setDelta(true);
        });

        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate( downloadBundle));
        assertEquals("The Delta version should be older than the Vocabulary version", exception.getMessage());
    }

    @Test
    void testSaveWithDeltaVersionEqualToVocabularyVersionForV5Deltas() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(2024_01_01);
            bundle.setDelta(true);
        });
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("The Delta version should be older than the Vocabulary version", exception.getMessage());
    }

    @Test
    void testSaveWithMissingVocabularyVersionInHistoryForV5Deltas() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(2022_01_01);
        });
        when(versionService.isPresentInHistory(downloadBundle.getVocabularyVersion())).thenReturn(false);
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("Vocabulary version is not found in the history.", exception.getMessage());
    }

    @Test
    void testSaveWithCurrentVersionMissingInHistoryForV5Deltas() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setName("TestBundle");
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(2022_01_01);
            bundle.setDelta(true);
        });
        when(versionService.isCurrentMissingInHistory(downloadBundle.getVocabularyVersion())).thenReturn(true);
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("The current version has not been uploaded to historical data. The delta cannot be created. Please contact the administrator.", exception.getMessage());
    }

    @Test
    void nameIsMissingValidationException() {
        DownloadBundle downloadBundle = Fn.create(DownloadBundle::new, bundle -> {
            bundle.setCdmVersion(CDMVersion.V5);
            bundle.setVocabularyVersion(2024_01_01);
            bundle.setDeltaVersion(2022_01_01);
            bundle.setDelta(true);
        });
        ValidationException exception = assertThrows(ValidationException.class, () ->
                bundleService.validate(downloadBundle));
        assertEquals("Please provide the bundle name", exception.getMessage());
    }


}