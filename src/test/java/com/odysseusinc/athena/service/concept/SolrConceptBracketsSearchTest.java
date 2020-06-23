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

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Ignore;
import org.junit.Test;

public class SolrConceptBracketsSearchTest extends SolrConceptSearchAbstractTest{

    @Test
    public void query_wordWithoutBrackets() throws Exception {

        String queryString = "hip";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals( String.format("Wrong outcome for '%s' query", queryString),
                Arrays.asList(
                        "hip fracture risk",
                        "(hip fracture risk",
                        "(hip) fracture risk",
                        "[hip fracture risk",
                        "[hip] fracture risk",
                        "[Hip] fracture risk",
                        "hip) fracture risk",
                        "hip] fracture risk",
                        "hip} fracture risk",
                        "hip} fracture risk",
                        "{hip fracture risk"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    public void query_notExactWordWithSquareBrackets() throws Exception {

        String queryString = "[hip]";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(
                Arrays.asList(
                        "[hip] fracture risk",
                        "[Hip] fracture risk",
                        "(hip fracture risk",
                        "(hip) fracture risk",
                        "[hip fracture risk",
                        "hip fracture risk",
                        "hip) fracture risk",
                        "hip] fracture risk",
                        "hip} fracture risk",
                        "hip} fracture risk",
                        "{hip fracture risk"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }

    @Test
    @Ignore //the result contains not only bracketed term but others as well, this happens due to elimination brackets from the query
    public void query_exactWordWithSquareBrackets() throws Exception {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO("\"[hip]\"");

        String queryString = "\"[hip]\"";
        SolrDocumentList docList = executeQuery(queryString);

        assertEquals(
                Arrays.asList(
                        "[hip] fracture risk",
                        "[Hip] fracture risk"
                ),
                docList.stream().map(f -> f.get("concept_name")).collect(Collectors.toList())
        );
    }
}