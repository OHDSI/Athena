package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

public class ConceptSolrQueryCreator {

    public static final String EXACT_TERM_REGEX = "\".+?\"";
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String FUZZY_SEARCH_FORMAT = "%s~";

    public String createSolrQueryString(ConceptSearchDTO source) {

        if (source.getQuery() == null || StringUtils.isEmpty(source.getQuery())) {
            return "*:*";
        }
        String phraseForQuery = source.getQuery()
                .trim()
                .replace("\\\"", "\""); //remove escaping quotes

        List<String> exactTerms = findExactTerms(phraseForQuery);
        List<String> notExactTerms = findNotExactTerms(phraseForQuery);

        String allTermsForWholePhraseQuery = Stream.concat(
                exactTerms.stream(),
                notExactTerms.stream().map(term -> String.format(FUZZY_SEARCH_FORMAT, term))
        ).collect(Collectors.joining(" AND "));

        String phraseWithoutQuotes = ClientUtils.escapeQueryChars(StringUtils.remove(phraseForQuery, "\""));
        String queryForWholePhrase = String.format(
                "%s OR concept_name_text:(%s)^7",
                getQueryForExactTerm(phraseWithoutQuotes, 9, 8, 8),
                allTermsForWholePhraseQuery
        );

        String queryForTermsFromPhrase = Stream
                .concat(
                        exactTerms.stream()
                                .map(term -> getQueryForExactTerm(term, 6, 5, 4)),
                        notExactTerms.stream()
                                .map(term -> getQueryForNotExactTerm(term))
                )
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));

        return String.format("(%s) OR (%s)",
                queryForWholePhrase,
                queryForTermsFromPhrase);

    }

    private List<String> findExactTerms(String queryString) {

        if (StringUtils.isEmpty(queryString) || StringUtils.isEmpty(queryString)) {
            return Collections.emptyList();
        }
        return findAllMatches(queryString, EXACT_TERM_REGEX).stream()
                .map(term -> term.substring(1, term.length() - 1)) //term is this best way to trim "
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
    }

    private List<String> findNotExactTerms(String queryString) {

        if (StringUtils.isEmpty(queryString) || StringUtils.isEmpty(queryString)) {
            return Collections.emptyList();
        }
        String stringWithoutExactTerms = queryString.replaceAll(EXACT_TERM_REGEX, StringUtils.EMPTY);
        return Arrays.stream(stringWithoutExactTerms.split(WHITESPACE_REGEX))
                .filter(StringUtils::isNotEmpty)
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

        return String.format("concept_name_ci:%1$s^6 OR " +
                "concept_name_ci:%1$s~0.6^5 OR " +
                "concept_name_text:%1$s^4 OR " +
                "concept_name_text:%1$s~^3 OR " +
                "concept_code_text:%1$s^3 OR " +
                "concept_code_text:*%1$s*^2 OR " +
                "query_wo_symbols:%1$s*", term);
    }

    private String getQueryForExactTerm(String term, int conceptNameCiPriority, int conceptCodeCiPriority, int priority) {
        //field "query" is specified in SOLR's managed-schema. It's type is "general text" which means that filters and tokenizers are applied to it
        //and other words may surround our term. We need an exact match, so here components of "query" are listed. Their type is String in SOLR that
        //guarantees an exact match.
        return String.format(
                "concept_name_ci:%1$s^" + conceptNameCiPriority + " OR " +
                        "concept_code_ci:%1$s^" + conceptCodeCiPriority + " OR " +
                        "id:%1$s^" + priority + " OR " +
                        "concept_code:%1$s^" + priority + " OR " +
                        "concept_name:%1$s^" + priority + " OR " +
                        "concept_class_id:%1$s^" + priority + " OR " +
                        "domain_id:%1$s^" + priority + " OR " +
                        "vocabulary_id:%1$s^" + priority + " OR " +
                        "standard_concept:%1$s^" + priority + " OR " +
                        "invalid_reason:%1$s^" + priority + " OR " +
                        "concept_synonym_name:%1$s^" + priority,
                term);
    }

}
