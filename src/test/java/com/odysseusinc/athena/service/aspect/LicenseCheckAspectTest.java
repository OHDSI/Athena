/*
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.odysseusinc.athena.service.aspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.repositories.v5.ConceptV5Repository;
import com.odysseusinc.athena.service.VocabularyConversionService;
import java.util.Optional;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class LicenseCheckAspectTest {

    private static final long MISSING_CONCEPT_ID = 214179L;

    @Test
    public void missingConceptIsReportedWithoutDereferencingJpaProxy() {

        ConceptV5Repository conceptRepository = mock(ConceptV5Repository.class);
        VocabularyConversionService conversionService = mock(VocabularyConversionService.class);
        LicenseCheckAspect aspect = new LicenseCheckAspect();
        ReflectionTestUtils.setField(aspect, "conceptRepository", conceptRepository);
        ReflectionTestUtils.setField(aspect, "vocabularyConversionService", conversionService);
        when(conceptRepository.findById(MISSING_CONCEPT_ID)).thenReturn(Optional.empty());

        NotExistException exception = assertThrows(NotExistException.class,
                () -> aspect.check(MISSING_CONCEPT_ID));

        assertEquals(ConceptV5.class, exception.getEntity());
        assertEquals("Concept with id 214179 does not exist", exception.getMessage());
        verifyNoInteractions(conversionService);
    }
}
