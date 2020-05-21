package com.odysseusinc.athena.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.remove;

public class ConceptSearchPhraseToSolrQueryService {

    public static final String EXACT_TERM_REGEX = "\".*?\"";
    public static final String WORD_DELIMITER_REGEX = "(\\s|\\/|\\?|!|,|;|\\.\\s|\\*)+";
    public static final List<String> SPEC_CHARS = Arrays.asList("\\", "+", "-", "!", "(", ")", ":", "^", "[", "]", "\"", "{", "}", "~", "*", "?", "|", "&", ";", "/");

    public static final String CONCEPT_ID = "concept_id";
    public static final String CONCEPT_NAME_CI = "concept_name_ci";
    public static final String CONCEPT_CODE_CI = "concept_code_ci";
    public static final String CONCEPT_CODE = "concept_code";
    public static final String CONCEPT_NAME = "concept_name";
    public static final String CONCEPT_CLASS_ID_CI = "concept_class_id_ci";
    public static final String DOMAIN_ID_CI = "domain_id_ci";
    public static final String VOCABULARY_ID_CI = "vocabulary_id_ci";
    public static final String CONCEPT_SYNONYM_NAME = "concept_synonym_name";
    public static final String CONCEPT_SYNONYM_NAME_CI = "concept_synonym_name_ci";

    public static final String CONCEPT_NAME_TEXT = "concept_name_text";
    public static final String CONCEPT_CODE_TEXT = "concept_code_text";
    public static final String CONCEPT_SYNONYM_NAME_TEXT = "concept_synonym_name_text";
    public static final String QUERY_SYMBOLS = "query";
    public static final String QUERY_WO_SYMBOLS = "query_wo_symbols";


    public String createQuery(ConceptSearchDTO source) {

        if (isBlank(source.getQuery())) {
            return "*:*";
        }
        String query = source.getQuery()
                .trim()
                .replace("\\\"", "\""); //remove escaping quotes
        QueryBoosts queryBoosts = getQueryBoosts(source.getBoosts());
        if (query.contains("*")) {
            return createAsteriskSolrQueryString(query, queryBoosts);
        } else {
            return createSolrQueryString(source, queryBoosts);
        }
    }

    public String createSolrQueryString(ConceptSearchDTO source, QueryBoosts queryBoosts) {

        String query = source.getQuery();

        List<String> exactTerms = findExactTerms(query);
        List<String> notExactTerms = findNotExactTerms(query);

        if (isEmpty(exactTerms) && isEmpty(notExactTerms)) {
            return "*:*";
        }
        return Stream.of(
                getQueryToFindDocsWithWholePhrase(query, queryBoosts.getPhrase()),
                getQueryToFindDocsWithAnyOfTermFromPhrase(exactTerms, notExactTerms, queryBoosts)
        )
                .filter(StringUtils::isNotBlank)
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));

    }

    protected List<String> findExactTerms(String phraseString) {

        if (isBlank(phraseString)) {
            return Collections.emptyList();
        }
        return findAllMatches(phraseString, EXACT_TERM_REGEX).stream()
                .map(term -> term.substring(1, term.length() - 1))
                .filter(StringUtils::isNotBlank)
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    protected List<String> findNotExactTerms(String phraseString) {

        if (isBlank(phraseString)) {
            return Collections.emptyList();
        }
        String stringWithoutExactTerms = phraseString.replaceAll(EXACT_TERM_REGEX, EMPTY);
        return Arrays.stream(stringWithoutExactTerms.split(WORD_DELIMITER_REGEX))
                .filter(StringUtils::isNotBlank)
                .filter(term -> !SPEC_CHARS.contains(term))
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    private String createAsteriskSolrQueryString(String query, QueryBoosts queryBoosts) {

        query = query.replaceAll("\\s+", " ");
        List<String> allTerms = Arrays.asList(query.split(" "));
        List<String> termsWithAsterisk = allTerms.stream()
                .filter(t -> t.contains("*"))
                .map(t -> t.substring(0, t.indexOf("*") + 1))
                .collect(Collectors.toList());

        final String asteriskQuery = buildAsteriskQuery(queryBoosts, termsWithAsterisk);

        List<String> otherTerms = ListUtils.subtract(allTerms, termsWithAsterisk);
        String otherTermsQuery = buildOtherTermsQuery(otherTerms, queryBoosts, asteriskQuery);

        return isBlank(otherTermsQuery) ? asteriskQuery : asteriskQuery + " OR " + otherTermsQuery;
    }

    private String buildOtherTermsQuery(List<String> otherTerms, QueryBoosts queryBoosts, String asteriskQuery) {

        Set<Set<String>> otherTermsCombinations = new HashSet<>();
        for (int termIndex = 1; termIndex <= otherTerms.size(); termIndex++) {
            otherTermsCombinations.addAll(Sets.combinations(new HashSet<>(otherTerms), termIndex));
        }
        return otherTermsCombinations.stream()
                .map(t -> {
                    String otherTermsQueryPart = t.stream()
                            .map(combination -> {
                                if (combination.startsWith("\"") && combination.endsWith("\"")) {
                                    combination = combination.substring(1, combination.length() - 1);
                                    return "(" + getQueryForExactTerm(combination, queryBoosts.getExactTerm()) + ")";
                                }
                                return "(" + getQueryForNotExactTerm(combination, queryBoosts.getNotExactTerm()) + ")";//otherTerms.size()
                            })
                            .collect(Collectors.joining(" AND "));

                    return "(" + asteriskQuery + " AND (" + otherTermsQueryPart + "))";
                })
                .collect(Collectors.joining(" OR "));
    }

    private String buildAsteriskQuery(QueryBoosts queryBoosts, List<String> termsWithAsterisk) {
        Collection<List<String>> asteriskTermsCombinations = Collections2.permutations(termsWithAsterisk);
        return "(" +
                asteriskTermsCombinations.stream()
                        .map(s -> String.join("\\ ", s))
                        .map(term -> getQueryForExactPhrase(term, queryBoosts.getPhrase()))
                        .collect(Collectors.joining(" OR "))
                + ")";
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

    private String getQueryToFindDocsWithWholePhrase(String phraseForQuery, QueryBoosts.PhraseBoosts boosts) {

        if (isBlank(phraseForQuery)) {
            return EMPTY;
        }
        String phraseWithoutQuotes = ClientUtils.escapeQueryChars(remove(phraseForQuery, "\""));
        return getQueryForExactPhrase(phraseWithoutQuotes, boosts);
    }

    private List<String> findAllMatches(String value, String regex) {

        if (isBlank(value) || isBlank(regex)) {
            return Collections.emptyList();
        }
        List<String> exacts = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(value);
        while (matcher.find()) {
            exacts.add(matcher.group());
        }
        return exacts;
    }

    private String getQueryForExactPhrase(String term, QueryBoosts.PhraseBoosts boosts) {
        //field "query" is specified in SOLR's managed-schema. It's type is "general text" which means that filters and tokenizers are applied to it
        //and other words may surround our term. We need an exact match, so here components of "query" are listed. Their type is String in SOLR that
        //guarantees an exact match.
        String boostedQuery = String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CLASS_ID_CI, term, boosts.getConceptClassIdCi()),
                String.format("%s:%s^%s", DOMAIN_ID_CI, term, boosts.getDomainIdCi()),
                String.format("%s:%s^%s", VOCABULARY_ID_CI, term, boosts.getVocabularyIdCi()));
        if (isNumeric(term)) {
            boostedQuery += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, boosts.getConceptId());
        }
        return boostedQuery;
    }

    private String getQueryToFindDocsWithAnyOfTermFromPhrase(List<String> exactTerms, List<String> notExactTerms, QueryBoosts queryBoosts) {

        String exactQueryPart = buildExactTermsQueryPart(exactTerms, queryBoosts);

        String notExactQueryPart = buildNotExactTermsQueryPart(notExactTerms, queryBoosts);

        if (isBlank(exactQueryPart)) {
            return notExactQueryPart;
        }
        if (isBlank(notExactQueryPart)) {
            return exactQueryPart;
        }
        // this is query for  "A and [B]" condition,
        // A is exact-part of the query
        // B is not exact part  and OPTIONAL
        return String.format("(%s) OR ((%s) AND (%s))", exactQueryPart, exactQueryPart, notExactQueryPart);
    }

    private String buildNotExactTermsQueryPart(List<String> notExactTerms, QueryBoosts queryBoosts) {

        final QueryBoosts.NotExactTermBoosts notExactTermBoosts = queryBoosts.getNotExactTerm();
        if (notExactTerms.size() < 2 && notExactTermBoosts.equals(QueryBoosts.buildDefault().getNotExactTerm())) {
            notExactTermBoosts.boostConceptCode();
        }

        return notExactTerms.stream()
                .filter(StringUtils::isNotBlank)
                .map(term -> getQueryForNotExactTerm(term, notExactTermBoosts))//, notExactTerms.size()
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));
    }

    private String buildExactTermsQueryPart(List<String> exactTerms, QueryBoosts queryBoosts) {

        return exactTerms.stream()
                .filter(StringUtils::isNotBlank)
                .map(term -> getQueryForExactTerm(term, queryBoosts.getExactTerm()))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));
    }

    private String getQueryForNotExactTerm(String term, QueryBoosts.NotExactTermBoosts boosts) {

        //0.7 - the required similarity of fuzzyness, see http://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Fuzzy%20Searches
        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s~0.7^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeTextFuzzy()),
                String.format("%s:%s^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameText()),
                String.format("%s:%s~0.7^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameTextFuzzy()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_TEXT, term, boosts.getConceptSynonymNameText()),
                String.format("%s:%s^%s", QUERY_WO_SYMBOLS, term, boosts.getQueryWoSymbols())
        );
    }

    private String getQueryForExactTerm(String term, QueryBoosts.ExactTermBoosts boosts) {

        String boostedQuery = String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", QUERY_SYMBOLS, term, boosts.getQuerySymbols()));
        if (isNumeric(term)) {
            boostedQuery += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, boosts.getConceptId());
        }
        return boostedQuery;
    }
}
