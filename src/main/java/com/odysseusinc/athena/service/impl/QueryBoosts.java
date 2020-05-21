package com.odysseusinc.athena.service.impl;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryBoosts {

    private NotExactTermBoosts notExactTerm;
    private ExactTermBoosts exactTerm;
    private PhraseBoosts phrase;

    public static QueryBoosts buildDefault(){

        return new QueryBoosts(new NotExactTermBoosts(), new ExactTermBoosts(), new PhraseBoosts());
    }


    @Data
    public static class NotExactTermBoosts {

        private Integer conceptCodeText = 50;
        private Integer conceptCodeTextFuzzy = 40;
        private Integer conceptNameText = 50;
        private Integer conceptNameTextFuzzy = 40;
        private Integer conceptSynonymNameText = 25;
        private Integer queryWoSymbols = 10;

        protected void boostConceptCode() {
            conceptCodeText = 80;
        }
    }

    @Data
    public static class ExactTermBoosts {
        private Integer conceptId = 70000;
        private Integer conceptCode = 60000;
        private Integer conceptName = 60000;
        private Integer conceptSynonymName = 40000;
        private Integer conceptCodeCi = 10000;
        private Integer conceptNameCi = 1000;
        private Integer conceptSynonymNameCi = 500;
        private Integer querySymbols = 1;
    }

    @Data
    public static class PhraseBoosts {
        private Integer conceptId = 70000;
        private Integer conceptCodeCi = 60000;
        private Integer conceptNameCi = 60000;
        private Integer conceptSynonymNameCi = 40000;
        private Integer conceptCode = 10000;
        private Integer conceptName = 1000;
        private Integer conceptSynonymName = 500;
        private Integer conceptClassIdCi = 100;
        private Integer domainIdCi = 100;
        private Integer vocabularyIdCi = 100;
    }
}
