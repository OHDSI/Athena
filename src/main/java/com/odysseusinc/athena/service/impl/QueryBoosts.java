package com.odysseusinc.athena.service.impl;


public class QueryBoosts {

    private NotExactTermBoosts notExactTerm = new NotExactTermBoosts();
    private ExactTermBoosts exactTerm = new ExactTermBoosts();
    private PhraseBoosts phrase = new PhraseBoosts();


    public NotExactTermBoosts getNotExactTerm() {

        return notExactTerm;
    }

    public void setNotExactTerm(NotExactTermBoosts notExactTerm) {

        this.notExactTerm = notExactTerm;
    }

    public ExactTermBoosts getExactTerm() {

        return exactTerm;
    }

    public void setExactTerm(ExactTermBoosts exactTerm) {

        this.exactTerm = exactTerm;
    }

    public PhraseBoosts getPhrase() {

        return phrase;
    }

    public void setPhrase(PhraseBoosts phrase) {

        this.phrase = phrase;
    }

    public static class NotExactTermBoosts {
        private Integer conceptCodeText = 100;
        private Integer conceptCodeTextFuzzy = 100;
        private Integer conceptNameText = 50;
        private Integer conceptNameTextFuzzy = 50;
        private Integer conceptSynonymName = 25;

        public Integer getConceptCodeText() {

            return conceptCodeText;
        }

        public void setConceptCodeText(Integer conceptCodeText) {

            this.conceptCodeText = conceptCodeText;
        }

        public Integer getConceptCodeTextFuzzy() {

            return conceptCodeTextFuzzy;
        }

        public void setConceptCodeTextFuzzy(Integer conceptCodeTextFuzzy) {

            this.conceptCodeTextFuzzy = conceptCodeTextFuzzy;
        }

        public Integer getConceptNameText() {

            return conceptNameText;
        }

        public void setConceptNameText(Integer conceptNameText) {

            this.conceptNameText = conceptNameText;
        }

        public Integer getConceptNameTextFuzzy() {

            return conceptNameTextFuzzy;
        }

        public void setConceptNameTextFuzzy(Integer conceptNameTextFuzzy) {

            this.conceptNameTextFuzzy = conceptNameTextFuzzy;
        }

        public Integer getConceptSynonymName() {

            return conceptSynonymName;
        }

        public void setConceptSynonymName(Integer conceptSynonymName) {

            this.conceptSynonymName = conceptSynonymName;
        }
    }

    public static class ExactTermBoosts {
        private Integer id = 100000;
        private Integer conceptCode = 10000;
        private Integer conceptName = 1000;
        private Integer conceptSynonymName = 500;
        private Integer querySymbols = 1;

        public Integer getId() {

            return id;
        }

        public void setId(Integer id) {

            this.id = id;
        }

        public Integer getConceptCode() {

            return conceptCode;
        }

        public void setConceptCode(Integer conceptCode) {

            this.conceptCode = conceptCode;
        }

        public Integer getConceptName() {

            return conceptName;
        }

        public void setConceptName(Integer conceptName) {

            this.conceptName = conceptName;
        }

        public Integer getConceptSynonymName() {

            return conceptSynonymName;
        }

        public void setConceptSynonymName(Integer conceptSynonymName) {

            this.conceptSynonymName = conceptSynonymName;
        }

        public Integer getQuerySymbols() {

            return querySymbols;
        }

        public void setQuerySymbols(Integer querySymbols) {

            this.querySymbols = querySymbols;
        }
    }

    public static class PhraseBoosts {
        private Integer id = 100000;
        private Integer conceptCodeCi = 80000;
        private Integer conceptNameCi = 60000;
        private Integer conceptSynonymNameCi = 40000;
        private Integer conceptCode = 10000;
        private Integer conceptName = 1000;
        private Integer conceptSynonymName = 500;
        private Integer conceptClassId = 100;
        private Integer domainId = 100;
        private Integer vocabularyId = 100;
        private Integer standardConcept = 100;
        private Integer invalidReason = 100;

        public Integer getId() {

            return id;
        }

        public void setId(Integer id) {

            this.id = id;
        }

        public Integer getConceptCodeCi() {

            return conceptCodeCi;
        }

        public void setConceptCodeCi(Integer conceptCodeCi) {

            this.conceptCodeCi = conceptCodeCi;
        }

        public Integer getConceptNameCi() {

            return conceptNameCi;
        }

        public void setConceptNameCi(Integer conceptNameCi) {

            this.conceptNameCi = conceptNameCi;
        }

        public Integer getConceptSynonymNameCi() {

            return conceptSynonymNameCi;
        }

        public void setConceptSynonymNameCi(Integer conceptSynonymNameCi) {

            this.conceptSynonymNameCi = conceptSynonymNameCi;
        }

        public Integer getConceptCode() {

            return conceptCode;
        }

        public void setConceptCode(Integer conceptCode) {

            this.conceptCode = conceptCode;
        }

        public Integer getConceptName() {

            return conceptName;
        }

        public void setConceptName(Integer conceptName) {

            this.conceptName = conceptName;
        }

        public Integer getConceptSynonymName() {

            return conceptSynonymName;
        }

        public void setConceptSynonymName(Integer conceptSynonymName) {

            this.conceptSynonymName = conceptSynonymName;
        }

        public Integer getConceptClassId() {

            return conceptClassId;
        }

        public void setConceptClassId(Integer conceptClassId) {

            this.conceptClassId = conceptClassId;
        }

        public Integer getDomainId() {

            return domainId;
        }

        public void setDomainId(Integer domainId) {

            this.domainId = domainId;
        }

        public Integer getVocabularyId() {

            return vocabularyId;
        }

        public void setVocabularyId(Integer vocabularyId) {

            this.vocabularyId = vocabularyId;
        }

        public Integer getStandardConcept() {

            return standardConcept;
        }

        public void setStandardConcept(Integer standardConcept) {

            this.standardConcept = standardConcept;
        }

        public Integer getInvalidReason() {

            return invalidReason;
        }

        public void setInvalidReason(Integer invalidReason) {

            this.invalidReason = invalidReason;
        }
    }
}
