package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptDetailsDTO;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import com.odysseusinc.athena.repositories.v5.ConceptV5Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.support.GenericConversionService;

import java.util.ArrayList;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConceptV5ToConceptDetailDTOConverterTest {

    @Mock
    private GenericConversionService conversionService;
    @Mock
    private ConceptV5Repository conceptV5Repository;
    @InjectMocks
    private ConceptV5ToConceptDetailDTOConverter converter;
    private ConceptV5 concept;

    @Before
    public void setUp() {
        concept = new ConceptV5();
        concept.setVocabulary(new VocabularyV5());
        concept.setSynonyms(new ArrayList<>());
    }

    @Test
    public void shouldMapToStandardConcept() {
        concept.setStandardConcept("S");

        final ConceptDetailsDTO conceptDto = converter.convert(concept);

        assertThat(conceptDto.getStandardConcept()).isEqualTo("Standard");
    }

    @Test
    public void shouldMaptoValid() {
        concept.setInvalidReason(null);

        final ConceptDetailsDTO conceptDto = converter.convert(concept);

        assertThat(conceptDto.getInvalidReason()).isEqualTo("Valid");
    }

    @Test
    public void shouldMapInvalidUtoUpgraded() {
        concept.setInvalidReason("U");

        final ConceptDetailsDTO conceptDto = converter.convert(concept);

        assertThat(conceptDto.getInvalidReason()).isEqualTo("Upgraded");
    }

    @Test
    public void shouldMapInvalidDtoDeleted() {
        concept.setInvalidReason("D");

        final ConceptDetailsDTO conceptDto = converter.convert(concept);

        assertThat(conceptDto.getInvalidReason()).isEqualTo("Deprecated");
    }

    @Test
    public void shouldMapInvalidtoDefault() {
        concept.setInvalidReason("X");

        final ConceptDetailsDTO conceptDto = converter.convert(concept);

        assertThat(conceptDto.getInvalidReason()).isEqualTo("Invalid (X)");
    }

}