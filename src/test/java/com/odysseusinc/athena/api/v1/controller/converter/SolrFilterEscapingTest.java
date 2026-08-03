/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
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
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller.converter;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Facet filter values ({@code domain}, {@code vocabulary}, {@code conceptClass},
 * {@code invalidReason}, {@code standardConcept}) come straight off the query string and
 * used to be interpolated raw between quotes, so a value containing a {@code "} closed the
 * quote and injected arbitrary Solr syntax into the filter query.
 * <p>
 * JUnit 4 on purpose.
 */
public class SolrFilterEscapingTest {

    private ConceptSearchDTOToSolrQuery converter;

    @Before
    public void setUp() {

        converter = new ConceptSearchDTOToSolrQuery(null, null, null, null);
    }

    private String filterQuery(String... values) {

        SolrQuery query = new SolrQuery();
        ReflectionTestUtils.invokeMethod(converter, "addFilter", values, "domain_id", query);
        String[] filters = query.getFilterQueries();
        assertEquals("expected exactly one filter query", 1, filters.length);
        return filters[0];
    }

    @Test
    public void ordinaryValuesAreQuotedAsBefore() {

        assertEquals("{!tag=DOMAIN_ID}domain_id:(\"Condition\")", filterQuery("Condition"));
        assertEquals("{!tag=DOMAIN_ID}domain_id:(\"Condition\" OR \"Drug\")",
                filterQuery("Condition", "Drug"));
    }

    /** The injection: a quote must not be able to break out of the quoted value. */
    @Test
    public void anEmbeddedQuoteCannotCloseTheQuotedValue() {

        String fq = filterQuery("Condition\") OR vocabulary_id:(\"CPT4");

        assertTrue("the quote must be escaped", fq.contains("\\\""));
        assertFalse("no unescaped quote may terminate the value early",
                fq.replace("\\\"", "").contains("\") OR"));
    }

    /** Solr boolean and grouping syntax must be inert inside a value. */
    @Test
    public void solrSyntaxInsideAValueIsEscaped() {

        String fq = filterQuery("a OR b", "c AND d", "e:f", "g(h)", "i*", "j?", "k~");

        for (String metachar : new String[]{"\\:", "\\(", "\\)", "\\*", "\\?", "\\~"}) {
            assertTrue("expected " + metachar + " to be escaped in: " + fq, fq.contains(metachar));
        }
    }

    @Test
    public void aNullValueInTheArrayIsSkippedRatherThanWrittenAsNull() {

        String fq = filterQuery("Condition", null);

        assertFalse("a null element must not be serialised", fq.contains("null"));
        assertEquals("{!tag=DOMAIN_ID}domain_id:(\"Condition\")", fq);
    }

    @Test
    public void theExcludeTagIsStillAppliedSoFacetCountsStayCorrect() {

        assertTrue(filterQuery("Condition").startsWith("{!tag=DOMAIN_ID}"));
    }
}
