/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptDetailsDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ShortConceptDTO;
import com.odysseusinc.athena.model.athenav5.ConceptSynonymV5;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import com.odysseusinc.athena.repositories.v5.ConceptV5Repository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConceptV5ToConceptDetailDTOConverter implements Converter<ConceptV5, ConceptDetailsDTO> {

    private ConceptV5Repository conceptV5Repository;

    private static final List<String> RX_NORM_VOCAB_IDS = Arrays.asList("RxNorm", "RxNorm Extension");

    @Autowired
    public ConceptV5ToConceptDetailDTOConverter(GenericConversionService conversionService,
                                                ConceptV5Repository conceptV5Repository) {

        conversionService.addConverter(this);
        this.conceptV5Repository = conceptV5Repository;
    }

    @Override
    @Transactional
    public ConceptDetailsDTO convert(ConceptV5 solrConcept) {

        ConceptDetailsDTO dto = new ConceptDetailsDTO();
        Long id = solrConcept.getId();
        dto.setId(id);
        dto.setName(solrConcept.getName());
        dto.setDomainId(solrConcept.getDomainId());
        dto.setConceptClassId(solrConcept.getConceptClassId());
        dto.setVocabularyId(solrConcept.getVocabularyId());
        dto.setConceptCode(solrConcept.getConceptCode());
        dto.setInvalidReason(solrConcept.getInvalidReason() == null ? "Valid" : "Invalid");
        String standard = solrConcept.getStandardConcept();
        dto.setStandardConcept(standard == null ? "Non-standard" : standard.equals("S") ? "Standard" : "Classification");
        dto.setSynonyms(solrConcept.getSynonyms().stream()
                .map(ConceptSynonymV5::getName)
                .collect(Collectors.toList()));
        dto.setValidStart(solrConcept.getValidStart());
        dto.setValidEnd(solrConcept.getValidEnd());

        VocabularyV5 vocabularyV5 = solrConcept.getVocabulary();
        dto.setVocabularyName(vocabularyV5.getName());
        dto.setVocabularyReference(vocabularyV5.getReference());
        dto.setVocabularyVersion(vocabularyV5.getVersion());

        if ("U".equals(solrConcept.getInvalidReason())) {
            String vocabularyId = solrConcept.getVocabularyId();
            List<String> vocabularyIds = RX_NORM_VOCAB_IDS.contains(vocabularyId) ? RX_NORM_VOCAB_IDS :
                    Collections.singletonList(vocabularyId);
            ConceptV5 updatedConcept = conceptV5Repository.findReplacedBy(id, vocabularyIds);
            if (updatedConcept != null) {
                dto.setValidTerm(new ShortConceptDTO(updatedConcept.getId(), updatedConcept.getName()));
            }
        }
        return dto;
    }
}
