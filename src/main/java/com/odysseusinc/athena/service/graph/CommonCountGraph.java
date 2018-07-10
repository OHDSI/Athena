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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.odysseusinc.athena.api.v1.controller.dto.relations.LinkDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.TermDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.support.GenericConversionService;

public class CommonCountGraph extends ConceptGraph {

    CommonCountGraph(Long id, GenericConversionService conversionService, List<ConceptAncestorRelationV5> relations) {

        super(id, conversionService, relations);
    }

    protected void initTermsAndLinks() {

        super.initTermsAndLinks();
        linkDtos = new ArrayList<>();
        termDtos = termDtos.stream()
                .collect(groupingBy(t -> {
                            int currentDepth = t.getDepth();
                            return currentDepth > 0 ? 1 : currentDepth < 0 ? -1 : 0;
                        },
                        mapping((TermDTO e) -> e, toList()))
                )
                .entrySet().stream()
                .map(this::buildCommonCountTerm)
                .collect(toList());

        termDtos.forEach(e -> {
            if (e.getDepth() == 1) {
                linkDtos.add(new LinkDTO(1L, 0L));
            } else if (e.getDepth() == -1) {
                linkDtos.add(new LinkDTO(0L, -1L));
            }
        });
    }

    private TermDTO buildCommonCountTerm(Map.Entry<Integer, List<TermDTO>> e) {

        TermDTO t = new TermDTO();
        t.setId(e.getKey().longValue());
        t.setName(e.getKey() == 0 ? e.getValue().get(0).getName() : String.valueOf(e.getValue().size()));
        t.setCurrent(e.getKey() == 0);
        t.setDepth(e.getKey());
        t.setCount(e.getValue().size());
        t.setWeight(e.getValue().size());
        t.setVocabularyId(e.getValue().get(0).getVocabularyId());
        t.setConceptClassId(e.getValue().get(0).getConceptClassId());
        return t;
    }
}
