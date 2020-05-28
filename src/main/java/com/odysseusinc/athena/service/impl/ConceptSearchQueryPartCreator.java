package com.odysseusinc.athena.service.impl;

import static org.apache.commons.lang3.StringUtils.isNumeric;

import org.springframework.stereotype.Component;

@Component
public class ConceptSearchQueryPartCreator {
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
    //0.7 - the required similarity of fuzzyness, see http://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Fuzzy%20Searches
    public static final String FUZZY_EDIT_DISTANCE = "0.7";

    public String notExactTerm(String term, QueryBoosts.NotExactTermBoosts boosts) {

        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s~%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeTextFuzzy(), FUZZY_EDIT_DISTANCE),
                String.format("%s:%s^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameText()),
                String.format("%s:%s~%s^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameTextFuzzy(), FUZZY_EDIT_DISTANCE),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_TEXT, term, boosts.getConceptSynonymNameText()),
                String.format("%s:%s^%s", QUERY_WO_SYMBOLS, term, boosts.getQueryWoSymbols())
        );
    }

    public String singleNotExactTerm(String term, QueryBoosts.SingleNotExactTermBoosts boosts) {

        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s~%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeTextFuzzy(), FUZZY_EDIT_DISTANCE)
        );
    }

    public String exactTerm(String term, QueryBoosts.ExactTermBoosts boosts) {

        String boostedQuery = String.join(" OR ",
                String.format("%s:\"%s\"^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:\"%s\"^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:\"%s\"^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:\"%s\"^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:\"%s\"^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:\"%s\"^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:\"%s\"^%s", QUERY_SYMBOLS, term, boosts.getQuerySymbols()));
        if (isNumeric(term)) {
            boostedQuery += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, boosts.getConceptId());
        }
        return boostedQuery;
    }

    public String singleExactTerm(String term, QueryBoosts.SingleExactTermBoosts boosts) {

        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi())
        );
    }

    public String asteriskTerm(String term, QueryBoosts.AsteriskTermBoosts boosts) {

        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s^%s", CONCEPT_NAME_TEXT, term, boosts.getConceptNameText()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_TEXT, term, boosts.getConceptSynonymNameText()));
    }

    public String singleAsteriskTerm(String term, QueryBoosts.AsteriskTermBoosts boosts) {

        return String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE_TEXT, term, boosts.getConceptCodeText()),
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi())
        );
    }

    public String wholePhrase(String term, QueryBoosts.PhraseBoosts boosts) {
        //field "query" is specified in SOLR's managed-schema. It's type is "general text" which means that filters and tokenizers are applied to it
        //and other words may surround our term. We need an exact match, so here components of "query" are listed. Their type is String in SOLR that
        //guarantees an exact match.
        String boostedQuery = String.join(" OR ",
                String.format("%s:%s^%s", CONCEPT_CODE, term, boosts.getConceptCode()),
                String.format("%s:%s^%s", CONCEPT_NAME, term, boosts.getConceptName()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME, term, boosts.getConceptSynonymName()),
                String.format("%s:%s^%s", CONCEPT_CODE_CI, term, boosts.getConceptCodeCi()),
                String.format("%s:%s^%s", CONCEPT_NAME_CI, term, boosts.getConceptNameCi()),
                String.format("%s:%s^%s", CONCEPT_SYNONYM_NAME_CI, term, boosts.getConceptSynonymNameCi()),
                String.format("%s:%s^%s", CONCEPT_CLASS_ID_CI, term, boosts.getConceptClassIdCi()),
                String.format("%s:%s^%s", DOMAIN_ID_CI, term, boosts.getDomainIdCi()),
                String.format("%s:%s^%s", VOCABULARY_ID_CI, term, boosts.getVocabularyIdCi()));
        if (isNumeric(term)) {
            boostedQuery += " OR " + String.format("%s:%s^%s", CONCEPT_ID, term, boosts.getConceptId());
        }
        return boostedQuery;
    }

}
