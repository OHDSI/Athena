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

package com.odysseusinc.athena.api.v1.controller.converter;

import static java.util.Arrays.asList;
import static org.hibernate.validator.internal.util.StringHelper.join;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.checker.LimitChecker;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConceptSearchDTOToSolrQuery {

    private static final String ID = "id";
    private static final String CLASS_ID = "concept_class_id";
    private static final String DOMAIN_ID = "domain_id";
    private static final String VOCABULARY_ID = "vocabulary_id";
    private static final String INVALID_REASON = "invalid_reason";
    private static final String STANDARD_CONCEPT = "standard_concept";

    @Autowired
    LimitChecker limitChecker;

    @Value("${solr.default.query.operator:AND}")
    private String solrQueryOperator;

    private void setSorting(ConceptSearchDTO source, SolrQuery result) {

        if (source.getSort() != null && source.getOrder() != null) {
            result.setSort(source.getSort(), SolrQuery.ORDER.valueOf(source.getOrder()));
        }
    }

    private void setIdOnlyOutput(SolrQuery result) {

        result.setFields("id");
    }

    private void setPagination(ConceptSearchDTO source, SolrQuery result) {

        if (source.getPage() != null && source.getPageSize() != null) {
            result.setStart((source.getPage() - 1) * source.getPageSize());
            result.setRows(source.getPageSize());
        }
    }

    private void setQuery(ConceptSearchDTO source, SolrQuery result) {

        String resultQuery = "*:*";
        String sourceQuery = source.getQuery().trim();
        if (!StringUtils.isEmpty(sourceQuery)) {
            sourceQuery = sourceQuery.replaceAll("[\"+\\-!(){}\\[\\]^~*?:&\\\\/]", "\\\\$0");
            List<String> splited = asList(StringUtils.split(sourceQuery));
            splited = splited.stream().map(e -> " query:" + e + "* ").collect(Collectors.toList());
            resultQuery = String.join(solrQueryOperator, splited);
        }
        result.setQuery(resultQuery);
    }

    private void setFilters(ConceptSearchDTO source, SolrQuery result) {

        addFilter(source.getConceptClass(), CLASS_ID, result);
        addFilter(source.getInvalidReason(), INVALID_REASON, result);
        addFilter(source.getStandardConcept(), STANDARD_CONCEPT, result);
        addFilter(source.getDomain(), DOMAIN_ID, result);
        addFilter(source.getVocabulary(), VOCABULARY_ID, result);
    }

    private void addFilter(String[] filter, String filterName, SolrQuery result) {

        if (filter != null) {
            result.addFilterQuery(getExcludedTag(filterName) + filterName
                    + ":" + "(\"" + join(filter, "\" OR \"") + "\")");
        }
    }

    private void setFacets(SolrQuery result) {

        result.addFacetField(DOMAIN_ID);
        result.addFacetField(CLASS_ID);
        result.addFacetField(VOCABULARY_ID);
        result.addFacetField(STANDARD_CONCEPT);
        result.addFacetField(INVALID_REASON);

        JSONObject jsonFacet = new JSONObject();
        putIntoJsonFacet(jsonFacet, DOMAIN_ID);
        putIntoJsonFacet(jsonFacet, CLASS_ID);
        putIntoJsonFacet(jsonFacet, VOCABULARY_ID);
        putIntoJsonFacet(jsonFacet, STANDARD_CONCEPT);
        putIntoJsonFacet(jsonFacet, INVALID_REASON);

        result.add("json.facet", jsonFacet.toString().replace("\"", ""));
    }

    private void putIntoJsonFacet(JSONObject jsonFacet, String facetField) {

        jsonFacet.put(getFacetLabel(facetField), new JSONObject()
                .put("type", "terms")
                .put("field", facetField)
                .put("limit", 100)
                .put("missing", true)
                .put("mincount", 0)
                .put("domain",
                        new JSONObject().put("excludeTags", getTag(facetField))));
    }

    private String getTag(String facetField) {

        return facetField.toUpperCase();
    }

    private String getExcludedTag(String facetField) {

        return String.format("{!tag=%s}", facetField.toUpperCase());
    }

    public static String getFacetLabel(String facetName) {

        return facetName + "s";
    }

    public SolrQuery createQuery(ConceptSearchDTO source) {

        SolrQuery result = baseQuery(source);
        setSorting(source, result);
        setPagination(source, result);
        setFacets(result);
        if (result.getFilterQueries() != null && result.getFilterQueries().length > 0) {
            result.setParam("facet.method", "fcs");
        } else {
            result.setParam("facet.method", "enum");
        }
        return result;
    }

    public SolrQuery convertForCursor(ConceptSearchDTO source) {

        SolrQuery result = baseQuery(source);
        result.setSort(ID, SolrQuery.ORDER.asc);
        result.setStart(0);
        result.setRows(limitChecker.getMaxLimitPageSize());
        return result;
    }

    private SolrQuery baseQuery(ConceptSearchDTO source) {

        SolrQuery result = new SolrQuery();
        setQuery(source, result);
        setFilters(source, result);
        return result;
    }
}
