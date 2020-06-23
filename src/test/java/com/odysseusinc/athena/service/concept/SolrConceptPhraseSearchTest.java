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
import com.odysseusinc.athena.service.impl.QueryDebugUtils;
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

public class SolrConceptPhraseSearchTest extends SolrConceptSearchAbstractTest {

    @Test
    public void query_wholePhrase() throws Exception {

        String queryString = "Stroke Myocardial Infarction Gastrointestinal Bleeding";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "Stroke",
                        "stroke"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_allWordFromPhrase() throws Exception {

        String queryString = "Stroke Bleeding Infarction Myocardial Gastrointestinal";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "Stroke",
                        "stroke"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_fewWords() throws Exception {

        String queryString = "Renal Dysfunction";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
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

        String queryString = "Gastrointestinal Bleeding";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
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

        String queryString = "\"Bleeding in Back\"";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction Bleeding in Back"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithFirstExactWord() throws Exception {

        String queryString = "\"Stroke\" Myocardial Infarction Gastrointestinal Bleeding";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Stroke Myocardial Infarction Strok",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "stroke"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithExactSubPhrase() throws Exception {

        String queryString = "Stroke Myocardial Infarction \"Gastrointestinal Bleeding\"";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Bleeding in Back Gastrointestinal Bleeding"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithComma() throws Exception {

        String queryString = "ibuprofen";
        SolrDocumentList docList = executeQuery(queryString);


        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "aspirin paracetamol ibuprofen",
                        "aspirin, paracetamol, ibuprofen"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_phraseWithExactCommaSearch() throws Exception {

        String queryString = "\"aspirin, paracetamol\"";
        SolrDocumentList docList = executeQuery(queryString);


        assertEquals(String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "aspirin paracetamol ibuprofen",
                        "aspirin, paracetamol, ibuprofen"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

}