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
 * Authors: Yaroslav Molodkov
 *
 */

package com.odysseusinc.athena.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ConceptSearchPhraseToSolrQueryServiceTestWithParams {

    private ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService =
            new ConceptSearchPhraseToSolrQueryService(new ConceptSearchQueryPartCreator());


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

    @Test
    public void extractTermsFromPhrase_bracketsQuotes() {
        String phraseString = "{May} {the} [Force] (be) with you";
        assertEquals(Collections.emptyList(), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("\\{May\\}", "\\{the\\}", "\\[Force\\]", "\\(be\\)", "with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_bracketsInExactTermQuotes() {
        String phraseString = "\"{May}\" \"{the}\" \"[Force]\" \"(be)\" with you";
        assertEquals(Arrays.asList("\\{May\\}", "\\{the\\}", "\\[Force\\]", "\\(be\\)"), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("with", "you"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

    @Test
    public void extractTermsFromPhrase_splitByDash() {
        String phraseString = "Winnie-the-Pooh - piglet";
        assertEquals(Collections.emptyList(), this.conceptSearchPhraseToSolrQueryService.findExactTerms(phraseString));
        assertEquals(Arrays.asList("Winnie\\-the\\-Pooh", "piglet"), this.conceptSearchPhraseToSolrQueryService.findNotExactTerms(phraseString));
    }

}