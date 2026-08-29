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
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@code setFacets} built a {@link JSONObject} and then stripped every quote out of
 * it before sending it. Solr's {@code json.facet} parser is lenient enough to accept that, so
 * it worked — but it meant deliberately breaking valid JSON, and a label or field name
 * containing a quote would have produced a malformed request rather than an escaped one.
 * <p>
 * The end-to-end proof that Solr accepts strict JSON is the embedded-Solr suite, which builds
 * its queries through {@code createQuery} and would fail on a rejected facet parameter. This
 * covers the property directly so the {@code replace} cannot quietly return.
 * <p>
 * JUnit 4 on purpose.
 */
public class SolrFacetJsonTest {

    private String facetJson() {

        SolrQuery query = new SolrQuery();
        ReflectionTestUtils.invokeMethod(
                new ConceptSearchDTOToSolrQuery(null, null, null, null), "setFacets", query);
        String json = query.get("json.facet");
        assertNotNull("json.facet should have been set", json);
        return json;
    }

    @Test
    public void theFacetParameterIsValidJson() {

        String json = facetJson();

        assertTrue("expected quoted JSON, got: " + json, json.contains("\""));
        new JSONObject(json); // throws JSONException if malformed
    }

    @Test
    public void everyFacetIsRequestedWithItsExcludeTag() {

        JSONObject json = new JSONObject(facetJson());

        for (String label : new String[]{"domain_ids", "concept_class_ids", "vocabulary_ids",
                "standard_concepts", "invalid_reasons"}) {
            JSONObject facet = json.getJSONObject(label);
            assertEquals("terms", facet.getString("type"));
            assertFalse("the exclude tag keeps facet counts correct",
                    facet.getJSONObject("domain").getString("excludeTags").isEmpty());
        }
    }
}
