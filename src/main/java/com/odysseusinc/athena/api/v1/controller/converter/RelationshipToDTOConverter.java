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

import com.odysseusinc.athena.api.v1.controller.dto.relations.RelationshipDTO;
import com.odysseusinc.athena.model.athenav5.RelationshipV5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class RelationshipToDTOConverter implements Converter<RelationshipV5, RelationshipDTO> {

    @Autowired
    public RelationshipToDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    public RelationshipDTO convert(RelationshipV5 relationship) {

        RelationshipDTO dto = new RelationshipDTO();
        dto.setId(relationship.getId());
        dto.setName(relationship.getName());
        return dto;
    }
}
