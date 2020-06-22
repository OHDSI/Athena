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
 * Authors: Yaroslav Molodkov
 *
 */
package com.odysseusinc.athena.service.concept;

import static com.odysseusinc.athena.service.concept.SolrTestUtils.createConceptSearchDTO;
import static org.apache.commons.lang3.tuple.ImmutableTriple.of;
import static org.junit.Assert.assertEquals;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService;
import com.odysseusinc.athena.service.impl.ConceptSearchQueryPartCreator;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class SolrConceptStandardAndValidFirstSearchTest {

    @ClassRule
    public static final TestRule serviceInitializer = SolrInitializer.INSTANCE;

    private ConceptSearchDTOToSolrQuery conceptSearchDTOToSolrQuery;
    private ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService;
    private ConceptSearchQueryPartCreator conceptSearchQueryPartCreator;

    @Before
    public void setUp() throws Exception {

        conceptSearchQueryPartCreator = new ConceptSearchQueryPartCreator();
        conceptSearchPhraseToSolrQueryService = new ConceptSearchPhraseToSolrQueryService(conceptSearchQueryPartCreator);

        conceptSearchDTOToSolrQuery = new ConceptSearchDTOToSolrQuery(
                conceptSearchPhraseToSolrQueryService,
                null,
                null,
                conceptSearchQueryPartCreator);

    }

    @Test
    public void query_wordWithoutBrackets() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Omeprazole");
        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());

        query.setParam("fl", "*,score");
        query.set("debugQuery", "on");

        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();
        assertEquals(
                Arrays.asList(
                        of("Omeprazole","Standard","Valid"),
                        of("Omeprazole","Classification","Valid"),
                        of("Omeprazole","Non-standard","Valid"),
                        of("Omeprazole","Non-standard","Invalid"),
                        of("omeprazole","Standard","Valid"),
                        of("omeprazole","Classification","Valid"),
                        of("omeprazole","Non-standard","Valid"),
                        of("omeprazole","Non-standard","Invalid")
                ),
                docList.stream()
                        .map(f -> of(f.get("concept_name"), f.get("standard_concept"), f.get("invalid_reason")))
                        .collect(Collectors.toList())
        );
    }

}