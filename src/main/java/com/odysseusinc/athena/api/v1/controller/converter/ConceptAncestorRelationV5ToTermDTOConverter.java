/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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

import com.odysseusinc.athena.api.v1.controller.dto.relations.TermDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ConceptAncestorRelationV5ToTermDTOConverter implements Converter<ConceptAncestorRelationV5, TermDTO> {

    @Autowired
    public ConceptAncestorRelationV5ToTermDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    public TermDTO convert(ConceptAncestorRelationV5 conceptAncestorRelationV5) {

        TermDTO dto = new TermDTO();
        dto.setId(conceptAncestorRelationV5.getId());
        dto.setCurrent(conceptAncestorRelationV5.getCurrent());
        dto.setName(conceptAncestorRelationV5.getName());
        dto.setWeight(conceptAncestorRelationV5.getWeight());
        dto.setDepth(conceptAncestorRelationV5.getDepth());
        dto.setVocabularyId(conceptAncestorRelationV5.getVocabularyId());
        dto.setConceptClassId(conceptAncestorRelationV5.getConceptClassId());
        return dto;
    }
}
