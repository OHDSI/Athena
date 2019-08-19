package com.odysseusinc.athena.service.impl;


import static org.junit.Assert.assertEquals;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ConceptSearchPhraseToSolrQueryServiceTest {

    private ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService = new ConceptSearchPhraseToSolrQueryService();
    @Test
    public void createSolrQueryString_withoutExactTerms() {

        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery("java is awesome");
        String queryString = conceptSearchPhraseToSolrQueryService.createSolrQueryString(searchDTO);
        assertEquals(
                "(concept_name_ci:java\\ is\\ awesome^9 OR concept_code_ci:java\\ is\\ awesome^8 OR id:java\\ is\\ awesome^8 OR concept_code:java\\ is\\ awesome^8 OR concept_name:java\\ is\\ awesome^8 OR concept_class_id:java\\ is\\ awesome^8 OR domain_id:java\\ is\\ awesome^8 OR vocabulary_id:java\\ is\\ awesome^8 OR standard_concept:java\\ is\\ awesome^8 OR invalid_reason:java\\ is\\ awesome^8 OR concept_synonym_name:java\\ is\\ awesome^8) OR " +
                        "(concept_name_text:(java~0.6 AND is~0.6 AND awesome~0.6)^7) OR " +
                        "((concept_name_text:java^4 OR concept_name_text:java~0.6^3 OR concept_code_text:java^3 OR concept_code_text:*java*~0.6^2 OR query_wo_symbols:java*) OR (concept_name_text:is^4 OR concept_name_text:is~0.6^3 OR concept_code_text:is^3 OR concept_code_text:*is*~0.6^2 OR query_wo_symbols:is*) OR (concept_name_text:awesome^4 OR concept_name_text:awesome~0.6^3 OR concept_code_text:awesome^3 OR concept_code_text:*awesome*~0.6^2 OR query_wo_symbols:awesome*))",
                queryString);
    }

    @Test
    public void createSolrQueryString_exactTermIsFirst() {

        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery("\"java\" is awesome");
        String queryString = conceptSearchPhraseToSolrQueryService.createSolrQueryString(searchDTO);
        assertEquals(
                "(concept_name_ci:java\\ is\\ awesome^9 OR concept_code_ci:java\\ is\\ awesome^8 OR id:java\\ is\\ awesome^8 OR concept_code:java\\ is\\ awesome^8 OR concept_name:java\\ is\\ awesome^8 OR concept_class_id:java\\ is\\ awesome^8 OR domain_id:java\\ is\\ awesome^8 OR vocabulary_id:java\\ is\\ awesome^8 OR standard_concept:java\\ is\\ awesome^8 OR invalid_reason:java\\ is\\ awesome^8 OR concept_synonym_name:java\\ is\\ awesome^8) OR " +
                        "(concept_name_text:(\"java\" AND is~0.6 AND awesome~0.6)^7) OR " +
                        "(((concept_name_text:\"java\"^4 OR concept_code_text:\"java\"^3 OR query_symbols:\"java\")) OR (((concept_name_text:\"java\"^4 OR concept_code_text:\"java\"^3 OR query_symbols:\"java\")) AND ((concept_name_text:is^4 OR concept_name_text:is~0.6^3 OR concept_code_text:is^3 OR concept_code_text:*is*~0.6^2 OR query_wo_symbols:is*) OR (concept_name_text:awesome^4 OR concept_name_text:awesome~0.6^3 OR concept_code_text:awesome^3 OR concept_code_text:*awesome*~0.6^2 OR query_wo_symbols:awesome*))))",
                queryString);
    }

    @Test
    public void createSolrQueryString_exactTermIsInTheMiddle() {

        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery("java \"is\" awesome");
        String queryString = conceptSearchPhraseToSolrQueryService.createSolrQueryString(searchDTO);
        assertEquals(
                "(concept_name_ci:java\\ is\\ awesome^9 OR concept_code_ci:java\\ is\\ awesome^8 OR id:java\\ is\\ awesome^8 OR concept_code:java\\ is\\ awesome^8 OR concept_name:java\\ is\\ awesome^8 OR concept_class_id:java\\ is\\ awesome^8 OR domain_id:java\\ is\\ awesome^8 OR vocabulary_id:java\\ is\\ awesome^8 OR standard_concept:java\\ is\\ awesome^8 OR invalid_reason:java\\ is\\ awesome^8 OR concept_synonym_name:java\\ is\\ awesome^8) OR " +
                        "(concept_name_text:(\"is\" AND java~0.6 AND awesome~0.6)^7) OR " +
                        "(((concept_name_text:\"is\"^4 OR concept_code_text:\"is\"^3 OR query_symbols:\"is\")) OR (((concept_name_text:\"is\"^4 OR concept_code_text:\"is\"^3 OR query_symbols:\"is\")) AND ((concept_name_text:java^4 OR concept_name_text:java~0.6^3 OR concept_code_text:java^3 OR concept_code_text:*java*~0.6^2 OR query_wo_symbols:java*) OR (concept_name_text:awesome^4 OR concept_name_text:awesome~0.6^3 OR concept_code_text:awesome^3 OR concept_code_text:*awesome*~0.6^2 OR query_wo_symbols:awesome*))))",
                queryString);
    }

    @Test
    public void createSolrQueryString_exactTermIsLast() {

        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery("java is \"awesome\"");
        String queryString = conceptSearchPhraseToSolrQueryService.createSolrQueryString(searchDTO);
        assertEquals(
                "(concept_name_ci:java\\ is\\ awesome^9 OR concept_code_ci:java\\ is\\ awesome^8 OR id:java\\ is\\ awesome^8 OR concept_code:java\\ is\\ awesome^8 OR concept_name:java\\ is\\ awesome^8 OR concept_class_id:java\\ is\\ awesome^8 OR domain_id:java\\ is\\ awesome^8 OR vocabulary_id:java\\ is\\ awesome^8 OR standard_concept:java\\ is\\ awesome^8 OR invalid_reason:java\\ is\\ awesome^8 OR concept_synonym_name:java\\ is\\ awesome^8) OR " +
                        "(concept_name_text:(\"awesome\" AND java~0.6 AND is~0.6)^7) OR " +
                        "(((concept_name_text:\"awesome\"^4 OR concept_code_text:\"awesome\"^3 OR query_symbols:\"awesome\")) OR (((concept_name_text:\"awesome\"^4 OR concept_code_text:\"awesome\"^3 OR query_symbols:\"awesome\")) AND ((concept_name_text:java^4 OR concept_name_text:java~0.6^3 OR concept_code_text:java^3 OR concept_code_text:*java*~0.6^2 OR query_wo_symbols:java*) OR (concept_name_text:is^4 OR concept_name_text:is~0.6^3 OR concept_code_text:is^3 OR concept_code_text:*is*~0.6^2 OR query_wo_symbols:is*))))",
                queryString);
    }

    @Test
    public void createSolrQueryString_wholePhraseIsExactTerm() {

        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery("\"java is awesome\"");
        String queryString = conceptSearchPhraseToSolrQueryService.createSolrQueryString(searchDTO);
        assertEquals(
                "(concept_name_ci:java\\ is\\ awesome^9 OR concept_code_ci:java\\ is\\ awesome^8 OR id:java\\ is\\ awesome^8 OR concept_code:java\\ is\\ awesome^8 OR concept_name:java\\ is\\ awesome^8 OR concept_class_id:java\\ is\\ awesome^8 OR domain_id:java\\ is\\ awesome^8 OR vocabulary_id:java\\ is\\ awesome^8 OR standard_concept:java\\ is\\ awesome^8 OR invalid_reason:java\\ is\\ awesome^8 OR concept_synonym_name:java\\ is\\ awesome^8) OR " +
                        "((concept_name_text:\"java\\ is\\ awesome\"^4 OR concept_code_text:\"java\\ is\\ awesome\"^3 OR query_symbols:\"java\\ is\\ awesome\"))",
                queryString);
    }

    @Test
    public void extractTermsFromPhrase_FirstWordIsExactTerm() {
        String phraseString = "\"May\" the Force be with you";
        assertEquals(Collections.singletonList("May"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("the", "Force", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_FirsTwoWordIsExactTerm() {
        String phraseString = "\"May the\" Force be with you";
        assertEquals(Collections.singletonList("May\\ the"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("Force", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_FirsTwoWordWithExtraSpaceIsExactTerm() {
        String phraseString = "\"May the \" Force be with you";
        assertEquals(Collections.singletonList("May\\ the\\ "), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList( "Force", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_WordInTheMiddleIsExactTerm() {
        String phraseString = "May the \"Force\" be with you";
        assertEquals(Collections.singletonList("Force"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("May", "the",  "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_LastWordIsExactTerm() {
        String phraseString = "May the Force be with \"you\"";
        assertEquals(Collections.singletonList("you"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("May", "the", "Force", "be", "with"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_AllWorksAreExactTerms() {
        String phraseString = "\"May\" \"the\" \"Force\" \"be\" \"with\" \"you\"";
        assertEquals(Arrays.asList("May", "the", "Force", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Collections.emptyList(), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_escapeChars() {
        String phraseString = "\"!May-the-Force-be\" with - y^ou! ";
        assertEquals(Collections.singletonList("\\!May\\-the\\-Force\\-be"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("with", "y\\^ou"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_emptyTerm() {
        String phraseString = "\"May\" \"\" the Force be with you";
        assertEquals(Collections.singletonList("May"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("the", "Force", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));

    }
    @Test
    public void extractTermsFromPhrase_oddAmountOfQuotes() {
        String phraseString = "\"May\" the Fo\"rce be with you";
        assertEquals(Collections.singletonList("May"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("the", "Fo\\\"rce", "be", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }
}