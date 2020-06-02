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

public class SolrConceptPhraseSearchTest {

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
                null
        );

    }

    @Test
    public void query_wholePhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Stroke Myocardial Infarction Gastrointestinal Bleeding");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(12, docList.size());
        assertEquals(
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "stroke",
                        "Stroke"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_allWordFromPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Stroke Bleeding Infarction Myocardial Gastrointestinal");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(12, docList.size());
        assertEquals(
                Arrays.asList(
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "stroke",
                        "Stroke"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_fewWords() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Renal Dysfunction");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(3, docList.size());
        assertEquals(
                Arrays.asList(
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_word() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Gastrointestinal Bleeding");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(5, docList.size());
        assertEquals(
                Arrays.asList(
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Bleeding in Back"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_exactPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"Bleeding in Back\"");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();
        assertEquals(2, docList.size());
        assertEquals(
                Arrays.asList(
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Bleeding in Back Gastrointestinal Bleeding"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithFirstExactWord() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"Stroke\" Myocardial Infarction Gastrointestinal Bleeding");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(11, docList.size());
        assertEquals(
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "stroke",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithExactSubPhrase() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Stroke Myocardial Infarction \"Gastrointestinal Bleeding\"");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();


        assertEquals(4, docList.size());
        assertEquals(
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Bleeding in Back Gastrointestinal Bleeding"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithComma() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("ibuprofen");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();


        assertEquals(2, docList.size());
        assertEquals(
                Arrays.asList(
                        "aspirin paracetamol ibuprofen",
                        "aspirin, paracetamol, ibuprofen"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithExactCommaSearch() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"aspirin, paracetamol\"");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();


        assertEquals(2, docList.size());
        assertEquals(
                Arrays.asList(
                        "aspirin paracetamol ibuprofen",
                        "aspirin, paracetamol, ibuprofen"
                        ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

}