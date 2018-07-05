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
import static java.util.stream.Collectors.toSet;

import com.odysseusinc.athena.api.v1.controller.dto.relations.LinkDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.TermDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.core.convert.support.GenericConversionService;

public class VocabularyGraph extends ConceptGraph {

    VocabularyGraph(Long id, GenericConversionService conversionService, List<ConceptAncestorRelationV5> relations) {

        super(id, conversionService, relations);
    }

    protected void initTermsAndLinks() {

        super.initTermsAndLinks();

        Set<Map.Entry<Integer, Map<String, List<TermDTO>>>> entrySet = termDtos.stream()
                .collect(groupingBy(t -> {
                            int currentDepth = t.getDepth();
                            return currentDepth > 0 ? 1 : currentDepth < 0 ? -1 : 0;
                        },
                        groupingBy(TermDTO::getVocabularyId, mapping((TermDTO e) -> e, toList()))))
                .entrySet();

        termDtos = new ArrayList<>();
        entrySet.forEach(e -> termDtos.addAll(getVocabularyTerms(e.getKey(), e.getValue())));

        linkDtos = new ArrayList<>();
        termDtos.stream()
                .filter(e -> e.getDepth() != 0)
                .forEach(e -> linkDtos.add(buildVocabularyLink(e.getDepth(), e.getId())));
    }

    private LinkDTO buildVocabularyLink(int depth, Long id) {

        return depth > 0 ? new LinkDTO(id, 0L) : new LinkDTO(0L, id);
    }

    private Collection<TermDTO> getVocabularyTerms(int depth, Map<String, List<TermDTO>> vocabularyToTerms) {

        return vocabularyToTerms.entrySet().stream()
                .map(e -> buildVocabularyTerm(depth, e))
                .collect(toSet());
    }

    protected static TermDTO buildVocabularyTerm(int depth, Map.Entry<String, List<TermDTO>> entry) {

        TermDTO t = new TermDTO();
        t.setId(depth == 0 ? 0 : new Random().nextLong());
        t.setName(depth == 0 ? entry.getValue().get(0).getName() : entry.getKey());
        t.setCurrent(depth == 0);
        t.setDepth(depth);
        int count = entry.getValue().size();
        t.setCount(count);
        t.setWeight(count);
        return t;
    }


}
