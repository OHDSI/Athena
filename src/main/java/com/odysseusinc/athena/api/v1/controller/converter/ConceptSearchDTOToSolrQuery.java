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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.api.v1.controller.converter;

import static java.util.Arrays.asList;
import static org.apache.solr.common.params.CommonParams.FQ;
import static org.hibernate.validator.internal.util.StringHelper.join;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.checker.LimitChecker;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConceptSearchDTOToSolrQuery {

    private static final String ID = "id";
    private static final String CLASS_ID = "concept_class_id";
    private static final String DOMAIN_ID = "domain_id";
    public static final String VOCABULARY_ID = "vocabulary_id";
    private static final String INVALID_REASON = "invalid_reason";
    private static final String STANDARD_CONCEPT = "standard_concept";

    @Autowired
    LimitChecker limitChecker;
    @Autowired
    VocabularyConversionService vocabularyConversionService;

    @Value("${solr.default.query.operator:AND}")
    private String solrQueryOperator;

    private void setSorting(ConceptSearchDTO source, SolrQuery result) {

        if (source.getSort() != null && source.getOrder() != null) {
            result.setSort(source.getSort(), SolrQuery.ORDER.valueOf(source.getOrder()));
        }
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

            //escaping all special query chars except whitespace
            sourceQuery = ClientUtils.escapeQueryChars(sourceQuery);

            boolean isExactMatch = sourceQuery.startsWith("\\\"") && sourceQuery.endsWith("\\\"");
            if (isExactMatch) {
                //this is "exact-matching" mode
                sourceQuery = sourceQuery.substring(2, sourceQuery.length() - 2);
                resultQuery = String.format(" concept_name:%1$s^3 OR concept_code:%1$s^2 OR id:%1$s " +
                        "OR concept_class_id:%1$s OR domain_id:%1$s OR vocabulary_id:%1$s OR standard_concept:%1$s " +
                        "OR invalid_reason:%1$s OR concept_synonym_name:%1$s", sourceQuery);
            } else {
                //here we specify priorities of searching fields
                resultQuery = String.format(" concept_name_ci:%1$s^8 OR concept_code_ci:%1$s^8", sourceQuery);

                List<String> split = asList(sourceQuery.split("\\\\ "));
                List<String> fuzzyTerms = split.stream()
                        .map(t -> t + "~")
                        .collect(Collectors.toList());
                resultQuery = resultQuery + " OR concept_name_text:(" + String.join(" AND ", fuzzyTerms) + ")^7";
                split = split.stream()
                        //here we specify priorities of searching fields 
                        .map(e -> String.format("(concept_name_ci:%1$s^6 OR " +
                                "concept_name_ci:%1$s~0.6^5 OR " +
                                "concept_name_text:%1$s^4 OR " +
                                "concept_name_text:%1$s~^3 OR " +
                                "concept_code_text:%1$s^3 OR " +
                                "concept_code_text:*%1$s*^2 OR query_wo_symbols:%1$s*)", e))
                        .collect(Collectors.toList());

                //the query string will be as follow:

                // concept_name_ci:aspirin^8 OR 
                // concept_code_ci:aspirin^8 OR 
                // concept_name_text:(aspirin~)^7 OR 
                // (concept_name_ci:aspirin^6 OR 
                //      concept_name_ci:aspirin~0.6^5 OR 
                //      concept_name_text:aspirin^4 OR 
                //      concept_name_text:aspirin~^3 OR 
                //      concept_code_text:aspirin^3 OR 
                //      concept_code_text:*aspirin*^2 OR 
                //      query_wo_symbols:aspirin*)

                //which means that the order of results will be:
                //for the whole phrase in query:
                //1) results with exact query string (case insensitive) in "concept name"
                //2) results with exact query string (case insensitive) in "concept code"
                //3) results with possible typos (case insensitive) in "concept name"
                //for split words in query:
                //4) results with exact query string (case insensitive) in "concept name"
                //5) results with possible typos (case insensitive) in "concept name"
                //6) results with exact query string (case insensitive) plus other words in "concept name"
                //7) results with exact query string (case insensitive) plus other words in "concept code"
                //8) results with partial matching of query string plus other words in "concept code"
                //9) results with partial matching of query string (regardless of brackets, parentheses and braces in "non-exact-matching mode") in "query"
                resultQuery = resultQuery + " OR " + String.join(solrQueryOperator, split);
            }
        }
        result.setQuery(resultQuery);
        result.setSort("score", SolrQuery.ORDER.desc);
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

    private void setUnavailableVocabularies(SolrQuery result, List<String> ids) {

        ids.forEach(e -> result.add(FQ, "-filter(" + VOCABULARY_ID + ":" + e + ")"));
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

    public SolrQuery createQuery(ConceptSearchDTO source, List<String> unavailableVocabularyIds) {

        List<String> ids = getWrappedInQuotationMarks(unavailableVocabularyIds);

        SolrQuery result = baseQuery(source, ids);
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

    public SolrQuery createQuery(ConceptSearchDTO source) {

        return createQuery(source, vocabularyConversionService.getUnavailableVocabularies());
    }

    public SolrQuery convertForCursor(ConceptSearchDTO source) {

        //quotation marks are for correct url query in case of compound vocabulary name
        List<String> ids = getWrappedInQuotationMarksUnavailableVocabularyIds();
        SolrQuery result = baseQuery(source, ids);
        result.setSort(ID, SolrQuery.ORDER.asc);
        result.setStart(0);
        result.setRows(limitChecker.getMaxLimitPageSize());
        return result;
    }

    private SolrQuery baseQuery(ConceptSearchDTO source, List<String> ids) {

        SolrQuery result = new SolrQuery();
        setQuery(source, result);
        setFilters(source, result);
        setUnavailableVocabularies(result, ids);
        return result;
    }

    private List<String> getWrappedInQuotationMarksUnavailableVocabularyIds() {

        List<String> v5Ids = vocabularyConversionService.getUnavailableVocabularies();
        return getWrappedInQuotationMarks(v5Ids);
    }

    private List<String> getWrappedInQuotationMarks(List<String> v5Ids) {

        return v5Ids.stream().map(e -> "\"" + e + "\"").collect(Collectors.toList());
    }
}
