package com.odysseusinc.athena.service.impl;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.remove;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.stereotype.Service;

@Service
public class ConceptSearchPhraseToSolrQueryService {

    public static final String EXACT_TERM_REGEX = "\".*?\"";
    public static final String WORD_DELIMITER_REGEX = "(\\s|\\/|\\?|!|,|;|\\.\\s)+";
    public static final List<String> SPEC_CHARS = Arrays.asList("\\", "+", "-", "!", "(", ")", ":", "^", "[", "]", "\"", "{", "}", "~", "*", "?", "|", "&", ";", "/");

    public static final String CONCEPT_CODE = "concept_code";
    public static final String CONCEPT_NAME = "concept_name";

    public static final String FIND_ALL_QUERY = "*:*";
    public static final String ASTERISK = "*";

    private ConceptSearchQueryPartCreator conceptSearchQueryPartCreator;

    public ConceptSearchPhraseToSolrQueryService(ConceptSearchQueryPartCreator conceptSearchQueryPartCreator) {

        this.conceptSearchQueryPartCreator = conceptSearchQueryPartCreator;
    }

    public String createQuery(ConceptSearchDTO source) {

        if (isBlank(source.getQuery())) {
            return FIND_ALL_QUERY;
        }
        return createSolrQueryString(source, getQueryBoosts(source.getBoosts()));
    }

    private String createSolrQueryString(ConceptSearchDTO source, QueryBoosts queryBoosts) {

        String query = source.getQuery();

        List<String> exactTerms = findExactTerms(query);
        List<String> notExactTerms = findNotExactTerms(query);
        List<String> asteriskTerms = findAsteriskTerms(query);

        if (isEmpty(exactTerms) && isEmpty(notExactTerms) && isEmpty(asteriskTerms)) {
            return FIND_ALL_QUERY;
        }
        return Stream.of(
                getQueryToFindDocsWithWholePhrase(query, queryBoosts.getPhrase()),
                getQueryToFindDocsWithSingleTerm(exactTerms, notExactTerms, asteriskTerms, queryBoosts),
                getQueryToFindDocsWithAnyOfTermFromPhrase(exactTerms, notExactTerms, asteriskTerms,  queryBoosts)

        )
                .filter(StringUtils::isNotBlank)
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));
    }

    private String getQueryToFindDocsWithSingleTerm(List<String> exactTerms, List<String> notExactTerms, List<String> asteriskTerms, QueryBoosts queryBoosts) {

        long totalTermAmount = Stream.of(exactTerms, notExactTerms, asteriskTerms)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .count();

        if (totalTermAmount == 1) {
            if (isNotEmpty(notExactTerms) && notExactTerms.size() == 1) {
                return conceptSearchQueryPartCreator.singleNotExactTerm(notExactTerms.get(0), queryBoosts.getSingleNotExactTermBoosts());
            }
            if (isNotEmpty(exactTerms) && exactTerms.size() == 1) {
                return conceptSearchQueryPartCreator.singleExactTerm(exactTerms.get(0), queryBoosts.getSingleExactTermBoosts());
            }
            if (isNotEmpty(asteriskTerms) && asteriskTerms.size() == 1) {
                return conceptSearchQueryPartCreator.singleAsteriskTerm(asteriskTerms.get(0), queryBoosts.getAsteriskTermBoosts());
            }
        }
        return EMPTY;
    }

    private String getQueryToFindDocsWithWholePhrase(String phraseForQuery, QueryBoosts.PhraseBoosts boosts) {

        if (isBlank(phraseForQuery)) {
            return EMPTY;
        }
        String phraseWithoutQuotes = remove(phraseForQuery, "\"");
        String phraseWithoutEscChars = ClientUtils.escapeQueryChars(phraseWithoutQuotes);
        String phraseWithoutDuplicatedWhitespaces = phraseWithoutEscChars.replaceAll("\\s+", " ");
        return conceptSearchQueryPartCreator.wholePhrase(phraseWithoutDuplicatedWhitespaces, boosts);
    }

    private String getQueryToFindDocsWithAnyOfTermFromPhrase(List<String> exactTerms, List<String> notExactTerms, List<String> asteriskTerms,  QueryBoosts queryBoosts) {

        String exactQueryPart = buildExactTermsQueryPart(exactTerms, queryBoosts);
        String notExactQueryPart = buildNotExactTermsQueryPart(notExactTerms, queryBoosts);
        String asteriskQueryPart = buildAsteriskTermsQueryPart(asteriskTerms, queryBoosts);

        String mandatoryPart = Stream.of(exactQueryPart, asteriskQueryPart)
                .filter(StringUtils::isNotEmpty)
                .map(part -> String.format("%s", part))
                .collect(Collectors.joining(" AND "));
        String optionalPart = notExactQueryPart;

        if (isBlank(mandatoryPart)) {
            return optionalPart;
        }
        if (isBlank(optionalPart)) {
            return mandatoryPart;
        }
        // the query for  "A + [B]" condition equals A || (A && B),
        // A - MANDATORY part of the query
        // B - OPTIONAL part of the query
        return String.format("%s OR (%s AND %s)", mandatoryPart, mandatoryPart, optionalPart);
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

    public List<String> findAsteriskTerms(String phraseString) {

        if (isBlank(phraseString)) {
            return Collections.emptyList();
        }
        String stringWithoutExactTerms = phraseString.replaceAll(EXACT_TERM_REGEX, EMPTY);
        return Arrays.stream(stringWithoutExactTerms.split(WORD_DELIMITER_REGEX))
                .filter(StringUtils::isNotBlank)
                .filter(term -> !(SPEC_CHARS.contains(term)))
                .filter(term -> StringUtils.endsWith(term, ASTERISK))
                .map(ClientUtils::escapeQueryChars)
                .map(term -> StringUtils.removeEnd(term, "\\*") + ASTERISK) //do not escape last asterisk
                .collect(Collectors.toList());
    }

    protected List<String> findNotExactTerms(String phraseString) {

        if (isBlank(phraseString)) {
            return Collections.emptyList();
        }

        String stringWithoutExactTerms = phraseString.replaceAll(EXACT_TERM_REGEX, EMPTY);
        return Arrays.stream(stringWithoutExactTerms.split(WORD_DELIMITER_REGEX))
                .filter(StringUtils::isNotBlank)
                .filter(term -> !(SPEC_CHARS.contains(term)))
                .filter(term -> !StringUtils.endsWith(term, ASTERISK))
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.toList());
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

    private String buildAsteriskTermsQueryPart(List<String> asteriskTerms, QueryBoosts queryBoosts) {

        return asteriskTerms.stream()
                .filter(StringUtils::isNotBlank)
                .map(term -> conceptSearchQueryPartCreator.asteriskTerm(term, queryBoosts.getAsteriskTermBoosts()))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));
    }

    private String buildNotExactTermsQueryPart(List<String> notExactTerms, QueryBoosts queryBoosts) {

        return notExactTerms.stream()
                .filter(StringUtils::isNotBlank)
                .map(term -> conceptSearchQueryPartCreator.notExactTerm(term, queryBoosts.getNotExactTerm()))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" OR "));
    }

    private String buildExactTermsQueryPart(List<String> exactTerms, QueryBoosts queryBoosts) {

        return exactTerms.stream()
                .filter(StringUtils::isNotBlank)
                .map(term -> conceptSearchQueryPartCreator.exactTerm(term, queryBoosts.getExactTerm()))
                .map(queryPart -> String.format("(%s)", queryPart))
                .collect(Collectors.joining(" AND "));
    }

}
