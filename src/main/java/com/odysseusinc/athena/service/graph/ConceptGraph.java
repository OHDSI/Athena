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

package com.odysseusinc.athena.service.graph;

import static java.util.stream.Collectors.toList;

import com.odysseusinc.athena.api.v1.controller.dto.relations.ConceptAncestorRelationsDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.LinkDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.ShortTermDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.TermDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.convert.support.GenericConversionService;

public class ConceptGraph implements RelationshipGraph {

    protected GenericConversionService conversionService;
    protected List<ConceptAncestorRelationV5> relations;

    protected Long id;

    protected List<TermDTO> termDtos;
    protected List<LinkDTO> linkDtos;

    ConceptGraph(Long id, GenericConversionService conversionService,
                 List<ConceptAncestorRelationV5> relations) {

        this.conversionService = conversionService;
        this.relations = relations;
        this.id = id;
    }

    public ConceptAncestorRelationsDTO build() {

        initTermsAndLinks();
        return getConceptAncestorRelationsDTO();
    }

    protected void initTermsAndLinks() {

        termDtos = relations.stream()
                .map(r -> conversionService.convert(r, TermDTO.class))
                .collect(toList());
        linkDtos = relations.stream()
                .map(r -> conversionService.convert(r, LinkDTO.class))
                .collect(toList());
    }

    private ConceptAncestorRelationsDTO getConceptAncestorRelationsDTO() {

        ConceptAncestorRelationsDTO result = new ConceptAncestorRelationsDTO();
        result.setTerms(termDtos.stream().map(ShortTermDTO::new).collect(Collectors.toCollection(LinkedHashSet::new)));
        result.setLinks(linkDtos);

        result.setConnectionsCount(relations.stream()
                .filter(e -> id.equals(e.getAncestorId()) || id.equals(e.getDescendantId()))
                .filter(e -> !e.getDescendantId().equals(e.getAncestorId()))
                .count());

        return result;
    }
}
