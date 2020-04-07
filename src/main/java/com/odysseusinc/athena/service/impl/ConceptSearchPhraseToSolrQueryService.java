package com.odysseusinc.athena.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

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

        String query = source.getQuery();
        QueryBoosts queryBoosts = getQueryBoosts(source.getBoosts());

        if (query == null || StringUtils.isEmpty(query)) {
            return "*:*";
        }
        query = query
                .trim()
                .replace("\\\"", "\""); //remove escaping quotes
        if (query.contains("*")) {
            return createAsteriskSolrQueryString(query, queryBoosts);
        } else {
            return createSolrQueryString(source, queryBoosts);
        }
    }

    private String createAsteriskSolrQueryString(String query, QueryBoosts queryBoosts) {

        query = query.replaceAll("\\s+", " ");
        List<String> allTerms = Arrays.asList(query.split(" "));
        List<String> termsWithAsterisk = allTerms.stream()
                .filter(t -> t.contains("*"))
                .map(t -> t.substring(0, t.indexOf("*") + 1))
                .collect(Collectors.toList());

        Collection<List<String>> asteriskTermsCombinations = Collections2.permutations(termsWithAsterisk);
        String asteriskQuery = "(" +
                asteriskTermsCombinations.stream()
                        .map(s -> String.join("\\ ", s))
                        .map(term -> getQueryForExactPhrase(term, queryBoosts))
                        .collect(Collectors.joining(" OR "))
                + ")";

        List<String> otherTerms = ListUtils.subtract(allTerms, termsWithAsterisk);
        Set<Set<String>> otherTermsCombinations = new HashSet<>();
        for (int i = 1; i <= otherTerms.size(); i++) {
            otherTermsCombinations.addAll(Sets.combinations(new HashSet<>(otherTerms), i));
        }
        String otherTermsQuery = otherTermsCombinations.stream()
                .map(t -> {
                    String otherTermsQueryPart = t.stream()
                            .map(e -> {
                                if (e.startsWith("\"") && e.endsWith("\"")) {
                                    e = e.substring(1, e.length() - 1);
                                    return "(" + getQueryForExactTerm(e, queryBoosts) + ")";
                                }
                                return "(" + getQueryForNotExactTerm(e, queryBoosts) + ")";
                            })
                            .collect(Collectors.joining(" AND "));

                    return "(" + asteriskQuery + " AND (" + otherTermsQueryPart + "))";
                })
                .collect(Collectors.joining(" OR "));
        return otherTermsQuery.isEmpty() ? asteriskQuery : asteriskQuery + " OR " + otherTermsQuery;
    }

    public String createSolrQueryString(ConceptSearchDTO source, QueryBoosts queryBoosts) {

        String query = source.getQuery();

        List<String> exactTerms = findExactTerms(query);
        List<String> notExactTerms = findNotExactTerms(query);

        if (CollectionUtils.isEmpty(exactTerms) && CollectionUtils.isEmpty(notExactTerms)) {
            return "*:*";
        }
        return Stream.of(
                getQueryToFindDocsWithWholePhrase(query, queryBoosts),
                getQueryToFindDocsWithAnyOfTermFromPhrase(exactTerms, notExactTerms, queryBoosts)
        )
                .filter(StringUtils::isNotEmpty)
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));
    }

    private QueryBoosts getQueryBoosts(String boostJson) {

        if (StringUtils.isEmpty(boostJson)) {
            return new QueryBoosts();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(boostJson, QueryBoosts.class);
        } catch (IOException e) {
            return new QueryBoosts();
        }
    }

    private String getQueryToFindDocsWithWholePhrase(String phraseForQuery, QueryBoosts queryBoosts) {

        if (StringUtils.isEmpty(phraseForQuery)) {
            return StringUtils.EMPTY;
        }
        String phraseWithoutQuotes = ClientUtils.escapeQueryChars(StringUtils.remove(phraseForQuery, "\""));
        return getQueryForExactPhrase(phraseWithoutQuotes, queryBoosts);
    }

    private String getQueryToFindDocsWithAnyOfTermFromPhrase(List<String> exactTerms, List<String> notExactTerms, QueryBoosts queryBoosts) {

        String exactQueryPart = exactTerms.stream()
                .filter(StringUtils::isNotEmpty)
                .map(term -> getQueryForExactTerm(term, queryBoosts))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));

        String notExactQueryPart = notExactTerms.stream()
                .filter(StringUtils::isNotEmpty)
                .map(term -> getQueryForNotExactTerm(term, queryBoosts))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));

        if (StringUtils.isEmpty(exactQueryPart)) {
            return notExactQueryPart;
        }
        if (StringUtils.isEmpty(notExactQueryPart)) {
            return exactQueryPart;
        }
        // this is query for  "A and [B]" condition,
        // A is exact-part of the query
        // B is not exact part  and OPTIONAL
        return String.format("(%s) OR ((%s) AND (%s))", exactQueryPart, exactQueryPart, notExactQueryPart);
    }

    protected List<String> findExactTerms(String phraseString) {

        if (StringUtils.isEmpty(phraseString)) {
            return Collections.emptyList();
        }
        return findAllMatches(phraseString, EXACT_TERM_REGEX).stream()
                .map(term -> term.substring(1, term.length() - 1))
                .filter(StringUtils::isNotEmpty)
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    protected List<String> findNotExactTerms(String phraseString) {

        if (StringUtils.isEmpty(phraseString)) {
            return Collections.emptyList();
        }
        String stringWithoutExactTerms = phraseString.replaceAll(EXACT_TERM_REGEX, StringUtils.EMPTY);
        return Arrays.stream(stringWithoutExactTerms.split(WORD_DELIMITER_REGEX))
                .filter(StringUtils::isNotEmpty)
                .filter(term -> !SPEC_CHARS.contains(term))
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    private List<String> findAllMatches(String value, String regex) {

        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(regex)) {
            return Collections.emptyList();
        }
        List<String> exacts = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(value);
        while (matcher.find()) {
            exacts.add(matcher.group());
        }
        return exacts;
    }


    private String getQueryForNotExactTerm(String term, QueryBoosts queryBoosts) {

        //0.7 - the required similarity of fuzzyness, see http://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Fuzzy%20Searches
        QueryBoosts.NotExactTermBoosts boosts = queryBoosts.getNotExactTerm();
        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s~0.7^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeTextFuzzy()),
                String.format("%s:%s^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameText()),
                String.format("%s:%s~0.7^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameTextFuzzy()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_TEXT, term, boosts.getConceptSynonymNameText()),
                String.format("%s:%s^%s", QUERY_WO_SYMBOLS, term, boosts.getQueryWoSymbols())
        );
    }

    private String getQueryForExactTerm(String term, QueryBoosts queryBoosts) {

        QueryBoosts.ExactTermBoosts boosts = queryBoosts.getExactTerm();
        return String.join(" OR ",
                String.format("%s:%s^%s", ID, term, boosts.getId()),
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", QUERY_SYMBOLS, term, boosts.getQuerySymbols()));
    }


    private String getQueryForExactPhrase(String term, QueryBoosts queryBoosts) {
        //field "query" is specified in SOLR's managed-schema. It's type is "general text" which means that filters and tokenizers are applied to it
        //and other words may surround our term. We need an exact match, so here components of "query" are listed. Their type is String in SOLR that
        //guarantees an exact match.
        QueryBoosts.PhraseBoosts query = queryBoosts.getPhrase();
        String boostedQuery = String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, query.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, query.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, query.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", CONCEPT_CODE, term, query.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, query.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, query.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CLASS_ID_CI, term, query.getConceptClassIdCi()),
                String.format("%s:%s^%s", DOMAIN_ID_CI, term, query.getDomainIdCi()),
                String.format("%s:%s^%s", VOCABULARY_ID_CI, term, query.getVocabularyIdCi()));
        if (StringUtils.isNumeric(term)) {
            boostedQuery += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, query.getId());
        }
        return boostedQuery;
    }

}
