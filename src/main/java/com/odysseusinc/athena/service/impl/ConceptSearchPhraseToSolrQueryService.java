package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

public class ConceptSearchPhraseToSolrQueryService {

    public static final String EXACT_TERM_REGEX = "\".*?\"";
    public static final String WORD_DELIMITER_REGEX = "(\\s|\\/|\\?|!|,|;|\\.\\s|\\*)+";
    public static final List<String> SPEC_CHARS = Arrays.asList("\\", "+", "-", "!", "(", ")", ":", "^", "[", "]", "\"", "{", "}", "~", "*", "?", "|", "&", ";", "/");
    public static final String FUZZY_FORMAT = "%s~0.6";
    public static final String EXACT_FORMAT = "\"%s\"";

    public static final String CONCEPT_ID = "concept_id";
    public static final String CONCEPT_NAME_CI = "concept_name_ci";
    public static final String CONCEPT_CODE_CI = "concept_code_ci";
    public static final String CONCEPT_CODE = "concept_code";
    public static final String CONCEPT_NAME = "concept_name";
    public static final String CONCEPT_CLASS_ID = "concept_class_id";
    public static final String DOMAIN_ID = "domain_id";
    public static final String VOCABULARY_ID = "vocabulary_id";
    public static final String STANDARD_CONCEPT = "standard_concept";
    public static final String INVALID_REASON = "invalid_reason";
    public static final String CONCEPT_SYNONYM_NAME = "concept_synonym_name";
    public static final String CONCEPT_SYNONYM_NAME_CI = "concept_synonym_name_ci";

    public static final String CONCEPT_NAME_TEXT = "concept_name_text";
    public static final String CONCEPT_CODE_TEXT = "concept_code_text";
    public static final String CONCEPT_SYNONYM_NAME_TEXT = "concept_synonym_name_text";
    public static final String QUERY_SYMBOLS = "query";
    public static final String QUERY_WO_SYMBOLS = "query_wo_symbols";


    public String createSolrQueryString(ConceptSearchDTO source) {

        if (source.getQuery() == null || StringUtils.isEmpty(source.getQuery())) {
            return "*:*";
        }
        String phraseForQuery = source.getQuery()
                .trim()
                .replace("\\\"", "\""); //remove escaping quotes

        List<String> exactTerms = findExactTerms(phraseForQuery);
        List<String> notExactTerms = findNotExactTerms(phraseForQuery);

        if (CollectionUtils.isEmpty(exactTerms) && CollectionUtils.isEmpty(notExactTerms)) {
            return "*:*";
        }
        return Stream.of(
                getQueryToFindDocsWithWholePhrase(phraseForQuery),
                getQueryToFindDocsWithAnyOfTermFromPhrase(exactTerms, notExactTerms)
        )
                .filter(StringUtils::isNotEmpty)
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));
    }

    private String getQueryToFindDocsWithWholePhrase(String phraseForQuery) {
        if (StringUtils.isEmpty(phraseForQuery)) {
            return StringUtils.EMPTY;
        }

        String phraseWithoutQuotes = ClientUtils.escapeQueryChars(StringUtils.remove(phraseForQuery, "\""));
        return getQueryForExactPhrase(phraseWithoutQuotes);
    }

    private String getQueryToFindDocsWithAnyOfTermFromPhrase(List<String> exactTerms, List<String> notExactTerms) {

        String exactQueryPart = exactTerms.stream()
                .filter(StringUtils::isNotEmpty)
                .map(this::getQueryForExactTerm)
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));

        String notExactQueryPart = notExactTerms.stream()
                .filter(StringUtils::isNotEmpty)
                .map(this::getQueryForNotExactTerm)
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

        if (StringUtils.isEmpty(phraseString) || StringUtils.isEmpty(phraseString)) {
            return Collections.emptyList();
        }

        return findAllMatches(phraseString, EXACT_TERM_REGEX).stream()
                .map(term -> term.substring(1, term.length() - 1))
                .filter(StringUtils::isNotEmpty)
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    protected List<String> findNotExactTerms(String phraseString) {

        if (StringUtils.isEmpty(phraseString) || StringUtils.isEmpty(phraseString)) {
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


    private String getQueryForNotExactTerm(String term) {
        //0.7 - the required similarity of fuzzyness, see http://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Fuzzy%20Searches
        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, 100),
                String.format("%s:%s~0.7^%s", CONCEPT_CODE_TEXT, term, 100),
                String.format("%s:%s^%s", CONCEPT_NAME_TEXT, term, 50),
                String.format("%s:%s~0.7^%s", CONCEPT_NAME_TEXT, term, 50),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_TEXT, term, 25));
    }

    private String getQueryForExactTerm(String term) {

        return String.join(" OR ",
                String.format("%s:\"%s\"^%s", ID, term, 100000),
                String.format("%s:\"%s\"^%s", CONCEPT_CODE, term, 10000),
                String.format("%s:\"%s\"^%s", CONCEPT_NAME, term, 1000),
                String.format("%s:\"%s\"^%s", CONCEPT_SYNONYM_NAME, term, 500),
                String.format("%s:\"%s\"", QUERY_SYMBOLS, term));
    }


    private String getQueryForExactPhrase(String term) {
        //field "query" is specified in SOLR's managed-schema. It's type is "general text" which means that filters and tokenizers are applied to it
        //and other words may surround our term. We need an exact match, so here components of "query" are listed. Their type is String in SOLR that
        //guarantees an exact match.
        String query =  String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, 80000),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, 60000),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, 40000),
                String.format("%s:%s^%s", CONCEPT_CODE, term, 10000),
                String.format("%s:%s^%s", CONCEPT_NAME, term, 1000),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, 500),
                String.format("%s:%s^%s", CONCEPT_CLASS_ID, term, 100),
                String.format("%s:%s^%s", DOMAIN_ID, term, 100),
                String.format("%s:%s^%s", VOCABULARY_ID, term, 100),
                String.format("%s:%s^%s", STANDARD_CONCEPT, term, 100),
                String.format("%s:%s^%s", INVALID_REASON, term, 100)
        );

        if (StringUtils.isNumeric(term)) {
            query += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, 100000);
        }
        return query;
    }

}
