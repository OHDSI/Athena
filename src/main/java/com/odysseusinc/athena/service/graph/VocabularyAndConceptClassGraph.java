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

package com.odysseusinc.athena.service.graph;

import static com.odysseusinc.athena.service.graph.VocabularyGraph.buildVocabularyTerm;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.odysseusinc.athena.api.v1.controller.dto.relations.LinkDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.TermDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.core.convert.support.GenericConversionService;

public class VocabularyAndConceptClassGraph extends ConceptGraph {

    VocabularyAndConceptClassGraph(Long id, GenericConversionService conversionService,
                                   List<ConceptAncestorRelationV5> relations) {

        super(id, conversionService, relations);
    }

    protected void initTermsAndLinks() {

        super.initTermsAndLinks();

        Set<Map.Entry<Integer, Map<String, List<TermDTO>>>> entrySet = termDtos.stream()
                .collect(groupingBy(t -> {
                            int depth1 = t.getDepth();
                            return depth1 > 0 ? 1 : depth1 < 0 ? -1 : 0;
                        },
                        groupingBy(TermDTO::getVocabularyId,
                                mapping((TermDTO e) -> e, toList()))))
                .entrySet();

        termDtos.clear();
        linkDtos.clear();
        entrySet.forEach(e -> addVocabularyTerms(e.getKey(), e.getValue()));
    }

    private Collection<TermDTO> getConceptClassTerms(TermDTO vocabulary, List<TermDTO> allVocabularyTerms) {

        Collection<TermDTO> conceptClassTerms = allVocabularyTerms.stream()
                .collect(groupingBy(TermDTO::getConceptClassId,
                        mapping((TermDTO e) -> e, toList())))
                .entrySet().stream()
                .map(e -> buildConceptClassTerm(vocabulary, e))
                .collect(toSet());

        conceptClassTerms.forEach(e -> {
            if (e.getDepth() < 0) {
                linkDtos.add(new LinkDTO(vocabulary.getId(), e.getId()));
            } else if (e.getDepth() > 0) {
                linkDtos.add(new LinkDTO(e.getId(), vocabulary.getId()));
            }
        });
        return conceptClassTerms;
    }

    private TermDTO buildConceptClassTerm(TermDTO vocabulary, Map.Entry<String, List<TermDTO>> entry) {

        TermDTO t = new TermDTO();
        t.setId(new Random().nextLong());
        t.setName(entry.getKey());
        t.setCurrent(false);
        t.setDepth(vocabulary.getDepth() > 0 ? 2 : -2);
        int count = entry.getValue().size();
        t.setCount(count);
        t.setWeight(count);
        return t;
    }

    private void addVocabularyTerms(int depth, Map<String, List<TermDTO>> vocabularyToTerms) {

        vocabularyToTerms.entrySet().forEach(e -> {

            TermDTO vocabularyTerm = buildVocabularyTerm(depth, e);
            termDtos.add(vocabularyTerm);

            if (depth == 1) {
                linkDtos.add(new LinkDTO(vocabularyTerm.getId(), 0L));
            } else if (depth == -1) {
                linkDtos.add(new LinkDTO(0L, vocabularyTerm.getId()));
            }
            if (depth != 0) {
                Collection<TermDTO> ccOfVocab = getConceptClassTerms(vocabularyTerm, e.getValue());
                termDtos.addAll(ccOfVocab);
            }
        });
    }

}
