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
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class SolrConceptPhraseSearchTest {

    @ClassRule
    public static final TestRule serviceInitializer = SolrInitializer.INSTANCE;

    private ConceptSearchDTOToSolrQuery conceptSearchDTOToSolrQuery = new ConceptSearchDTOToSolrQuery();

    @Test
    public void query_phrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Pooh eats honey");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(13, docList.size());
        assertEquals(
                Arrays.asList(
                        "Pooh eats honey",
                        "honey eats Pooh",
                        "Pooh eats raspberries and honey",
                        "Pooh steals honey",
                        "pooh eats",
                        "pooh eats pooh",
                        "pooh eats nothing",
                        "Pooh eats raspberries",
                        "Pooh eats raspberries and me",
                        "Piglet hates honey",
                        "pooh",
                        "Pooh",
                        "pooo"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_allWordFromPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("honey eats pooh");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(13, docList.size());
        assertEquals(
                Arrays.asList(
                        "honey eats Pooh",
                        "Pooh eats honey",
                        "Pooh eats raspberries and honey",
                        "Pooh steals honey",
                        "pooh eats",
                        "pooh eats pooh",
                        "pooh eats nothing",
                        "Pooh eats raspberries",
                        "Pooh eats raspberries and me",
                        "Piglet hates honey",
                        "pooh",
                        "Pooh",
                        "pooo"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_exactPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"Pooh eats honey\"");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(1, docList.size());
        assertEquals(
                Arrays.asList("Pooh eats honey"),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithFirstExactWord() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"pooh\" eats honey");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(11, docList.size());
        assertEquals(
                Arrays.asList(
                        "Pooh eats honey",
                        "honey eats Pooh",
                        "Pooh eats raspberries and honey",
                        "pooh eats pooh",
                        "Pooh steals honey",
                        "pooh eats",
                        "pooh eats nothing",
                        "Pooh eats raspberries",
                        "Pooh eats raspberries and me",
                        "pooh",
                        "Pooh"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithOneExactWord() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Pooh \"eats\" honey");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(8, docList.size());
        assertEquals(
                Arrays.asList(
                        "Pooh eats honey",
                        "honey eats Pooh",
                        "Pooh eats raspberries and honey",
                        "pooh eats",
                        "pooh eats pooh",
                        "pooh eats nothing",
                        "Pooh eats raspberries",
                        "Pooh eats raspberries and me"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithExactSubPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"Pooh eats\" honey");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(7, docList.size());
        assertEquals(
                Arrays.asList(
                        "Pooh eats honey",
                        "Pooh eats raspberries and honey",
                        "pooh eats",
                        "pooh eats nothing",
                        "pooh eats pooh",
                        "Pooh eats raspberries",
                        "Pooh eats raspberries and me"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }



}