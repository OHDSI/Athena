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

import static com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService.CONCEPT_CODE;
import static com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService.CONCEPT_NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.solr.common.params.CommonParams.FQ;
import static org.hibernate.validator.internal.util.StringHelper.join;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.checker.LimitChecker;
import com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService;

import com.odysseusinc.athena.service.impl.ConceptSearchQueryPartCreator;
import com.odysseusinc.athena.service.impl.QueryBoosts;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component

public class ConceptSearchDTOToSolrQuery {

    private static final Logger log = LoggerFactory.getLogger(ConceptSearchDTOToSolrQuery.class);

    public static final String DOMAIN_ID = "domain_id";
    public static final String VOCABULARY_ID = "vocabulary_id";
    private static final String CONCEPT_ID = "concept_id";
    private static final String CLASS_ID = "concept_class_id";
    private static final String INVALID_REASON = "invalid_reason";
    private static final String STANDARD_CONCEPT = "standard_concept";
    private static final String CASE_INSENSITIVE_SUFFIX = "_ci";
    private static final List<String> CASE_INSENSITIVE_FIELDS = Arrays.asList(CONCEPT_CODE, CONCEPT_NAME);

    private final ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService;
    private final LimitChecker limitChecker;
    private final VocabularyConversionService vocabularyConversionService;
    private final ConceptSearchQueryPartCreator conceptSearchQueryPartCreator;

    @Autowired
    public ConceptSearchDTOToSolrQuery(ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService,
                                       @Lazy LimitChecker limitChecker,
                                       VocabularyConversionService vocabularyConversionService,
                                       ConceptSearchQueryPartCreator conceptSearchQueryPartCreator) {

        this.conceptSearchPhraseToSolrQueryService = conceptSearchPhraseToSolrQueryService;
        this.limitChecker = limitChecker;
        this.vocabularyConversionService = vocabularyConversionService;
        this.conceptSearchQueryPartCreator = conceptSearchQueryPartCreator;
    }

    public static String getFacetLabel(String facetName) {

        return facetName + "s";
    }

    private void setSorting(ConceptSearchDTO source, SolrQuery result) {

        if (source.getSort() != null && source.getOrder() != null) {
            result.setSort(getSortFieldWithSuffix(source.getSort()), SolrQuery.ORDER.valueOf(source.getOrder()));
        }
    }

    private String getSortFieldWithSuffix(String field) {

        if (CASE_INSENSITIVE_FIELDS.contains(field)) {
            return field + CASE_INSENSITIVE_SUFFIX;
        }
        return field;
    }

    private void setPagination(ConceptSearchDTO source, SolrQuery result) {

        if (source.getPage() != null && source.getPageSize() != null) {
            result.setStart((source.getPage() - 1) * source.getPageSize());
            result.setRows(source.getPageSize());
        }
    }

    private void setQuery(ConceptSearchDTO source, SolrQuery query, QueryBoosts queryBoosts) {

        String queryString = conceptSearchPhraseToSolrQueryService.createQuery(source, queryBoosts);

        log.debug("Concept search query: {}", queryString);

        query.setQuery(queryString);
        SortClause sortByScore = new SortClause("score", SolrQuery.ORDER.desc);
        SortClause sortByConceptName = new SortClause("concept_name_ci", SolrQuery.ORDER.asc);
        query.setSort(sortByScore);
        query.addSort(sortByConceptName);
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

    public SolrQuery createQuery(ConceptSearchDTO source, List<String> unavailableVocabularyIds) {
        List<String> ids = getWrappedInQuotationMarks(unavailableVocabularyIds);

        QueryBoosts queryBoosts = getQueryBoosts(source.getBoosts());
        SolrQuery query = baseQuery(source, ids, queryBoosts);

        setSorting(source, query);
        setPagination(source, query);
        setAdditionalPriorities(query, queryBoosts);
        setFacets(query);
        if (query.getFilterQueries() != null && query.getFilterQueries().length > 0) {
            query.setParam("facet.method", "fcs");
        } else {
            query.setParam("facet.method", "enum");
        }
        return query;
    }

    private void setAdditionalPriorities(SolrQuery query, QueryBoosts queryBoosts) {

        query.set("defType", "edismax");
        query.set("bq", conceptSearchQueryPartCreator.additionalPriority(queryBoosts.getAdditionalBoosts()));
    }

    public SolrQuery createQuery(ConceptSearchDTO source) {

        return createQuery(source, vocabularyConversionService.getUnavailableVocabularies());
    }

    public SolrQuery convertForCursor(ConceptSearchDTO source) {

        //quotation marks are for correct url query in case of compound vocabulary name
        List<String> ids = getWrappedInQuotationMarksUnavailableVocabularyIds();
        QueryBoosts queryBoosts = getQueryBoosts(source.getBoosts());
        SolrQuery result = baseQuery(source, ids, queryBoosts);
        result.setSort(CONCEPT_ID, SolrQuery.ORDER.asc);
        result.setStart(0);
        result.setRows(limitChecker.getMaxLimitPageSize());
        return result;
    }

    private QueryBoosts getQueryBoosts(String boostJson) {

        if (isBlank(boostJson)) {
            return QueryBoosts.buildDefault();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(boostJson, QueryBoosts.class);
        } catch (IOException e) {
            return QueryBoosts.buildDefault();
        }
    }

    private SolrQuery baseQuery(ConceptSearchDTO source, List<String> ids, QueryBoosts queryBoosts) {

        SolrQuery result = new SolrQuery();
        setQuery(source, result, queryBoosts);
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
