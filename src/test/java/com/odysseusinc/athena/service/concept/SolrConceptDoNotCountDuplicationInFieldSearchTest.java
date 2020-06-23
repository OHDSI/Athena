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
import static org.junit.Assert.assertEquals;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService;
import com.odysseusinc.athena.service.impl.ConceptSearchQueryPartCreator;
import com.odysseusinc.athena.service.support.TestQueryDebugUtils;
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

public class SolrConceptDoNotCountDuplicationInFieldSearchTest {

    @ClassRule
    public static final TestRule serviceInitializer = SolrInitializer.INSTANCE;

    private ConceptSearchDTOToSolrQuery conceptSearchDTOToSolrQuery;

    @Before
    public void setUp() throws Exception {

        ConceptSearchQueryPartCreator conceptSearchQueryPartCreator = new ConceptSearchQueryPartCreator();
        ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService =
                new ConceptSearchPhraseToSolrQueryService(conceptSearchQueryPartCreator);

        conceptSearchDTOToSolrQuery = new ConceptSearchDTOToSolrQuery(
                conceptSearchPhraseToSolrQueryService,
                null,
                null,
                conceptSearchQueryPartCreator
        );

    }

    @Test
    public void query_repeatingTermsDoesNotIncreasePriority() throws Exception {

        String queryString = "Lisinopril Oral";

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO(queryString);

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());

        QueryResponse response = TestQueryDebugUtils.debug("repeatingTermsDoesNotIncreasePriority", query, () -> SolrInitializer.server.query(query));
        SolrDocumentList docList = response.getResults();

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Lisinopril Oral",
                        "Lisinopril Oral Suspension",
                        "Lisinopril Oral Tablet [Lisinopril Oral]",
                        "Lisinopril ORAL 20020701 TABLET [Lisinopril]"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }



}