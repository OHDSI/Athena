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

public class SolrConceptFuzzySearchTest {

    @ClassRule
    public static final TestRule serviceInitializer = SolrInitializer.INSTANCE;

    private ConceptSearchDTOToSolrQuery conceptSearchDTOToSolrQuery = new ConceptSearchDTOToSolrQuery();

    @Test
    public void query_fuzzy() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("Strok Myocardi8 Infarctiin Gastrointestinal Bleedi");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(13, docList.size());
        assertEquals(
                Arrays.asList(
                        "Gastrointestinal Bleeding Myocardial Infarction Stroke",
                        "Stroke Myocardial Infarction Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction",
                        "Stroke Myocardial Infarction Strok",
                        "Bleeding in Back Gastrointestinal Bleeding",
                        "Stroke Myocardial Infarction Bleeding in Back",
                        "Stroke Myocardial Infarction",
                        "Stroke Myocardial Infarction Stroke Nothin",
                        "Stroke Myocardial Infarction  Renal Dysfunction",
                        "Stroke Myocardial Infarction Renal Dysfunction and Nothing",
                        "stroke",
                        "Stroke",
                        "Strook"
                        ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_TooFuzzy() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("P888");

        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());
        QueryResponse response = SolrInitializer.server.query(query);
        SolrDocumentList docList = response.getResults();

        assertEquals(0, docList.size());
    }


    private ConceptSearchDTO createConceptSearchDTO(String searchString) {

        ConceptSearchDTO conceptSearchDTO = new ConceptSearchDTO();
        conceptSearchDTO.setQuery(searchString);
        conceptSearchDTO.setPage(1);
        conceptSearchDTO.setPageSize(30);
        return conceptSearchDTO;
    }



}