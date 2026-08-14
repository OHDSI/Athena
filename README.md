# Athena

Athena is the OHDSI vocabulary distribution service: it lets users search the standardised vocabularies
(concepts, synonyms, relationships) and download them as versioned CDM v5 bundles.

This repository is the back end - a Spring Boot application that serves both the REST API and
the compiled front end. The UI lives in [OHDSI/AthenaUI](https://github.com/OHDSI/AthenaUI)
and is included here as the `ui` submodule, built from source during packaging.

| | |
|---|---|
| Runtime | Java 25, Spring Boot 4.1 |
| Build | Maven |
| Search | Apache Solr (core `concepts`) |
| Storage | PostgreSQL |
| Auth | SAML SSO |

---

## Getting started

### Prerequisites

- JDK 25 or later
- Docker - the test suite starts PostgreSQL containers, and it is the easiest way to run the
  services locally
- PostgreSQL with four databases: `athena_db`, `athena_cdm_v4_5`, `athena_cdm_v5` (port 5432)
  and `athena_cdm_v5_history` (port 5433). Flyway creates every schema on first start, so they
  only need to exist and be empty.
- Solr with a `concepts` core on port 8984. Its configuration is assembled from two trees:
  `src/main/resources/solr` supplies `managed-schema` and `solrconfig.xml`, but that schema
  references `lang/*.txt`, `stopwords.txt` and `synonyms.txt`, which live under
  `src/test/resources`. Neither is a complete configset on its own.

### Build and run

```bash
# Full build: compiles the UI submodule and packages it into the jar
git submodule update --init
mvn package
java -jar target/athena.jar

# Back end only, skipping the front-end build
mvn package -PskipUi

# Run from source
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application listens on **http://localhost:3010** and serves the UI and the API together.
When working on the front end, run AthenaUI's dev server instead - it proxies `/api` and
`/auth/*` here.

Use `-PskipUi` whenever you only want the API: without it the build runs `npm ci && npm run
build` in `ui/`, which is slow and pointless if the front end is being served elsewhere.

### Configuration

Profiles live in `properties/{dev,qa,prod,test}/`; the selected one is copied into the jar at
build time. `dev` is the default. The settings worth knowing are the four datasources
(`spring.datasource-db`, `-v4`, `-v5`, `-v5-history`), `athena.solrServerUrl`, and the SAML
block under `cas.*`.

> No SAML keystore ships with the application. To exercise SSO locally, supply your own and
> point `cas.key-manager.key-store-file` at it.

### Tests

```bash
mvn test          # requires a running Docker engine
```

The suite starts four PostgreSQL testcontainers and an embedded Solr; no external services are
needed. Tests must leave the working tree unmodified - CI enforces this.

---

## Search behaviour

How a query string is interpreted. The authoritative implementation is
`ConceptSearchDTOToSolrQuery`.

Search runs across all concept fields, but `CONCEPT_NAME` and `CONCEPT_CODE` carry the highest
priority.

### Phrase search

Results are ordered by: full phrase match first, then concepts containing all the words, then
by how many of the searched words matched and how important each is (rarer words across the
corpus count for more).

For **Stroke Myocardial Infarction Gastrointestinal Bleeding**:

| Name | Why it ranks there |
|---|---|
| Stroke Myocardial Infarction Gastrointestinal Bleeding | full match |
| Gastrointestinal Bleeding Myocardial Infarction Stroke | all words |
| Stroke Myocardial Infarction Gastrointestinal Bleeding and Renal Dysfunction | 5 words |
| Stroke Myocardial Infarction Bleeding in Back | 4 words |
| Stroke Myocardial Infarction Renal Dysfunction and Nothing | 3 words |

### Exact search

Quotation marks force an exact match: the word must be present, and stemming is disabled, so it
must appear exactly as quoted. Case and the number of spaces between words are still ignored.

`"Stroke Myocardial Infarction Gastrointestinal Bleeding"` matches only names containing that
exact sequence. Quoting part of a query - `Stroke Myocardial Infarction "Gastrointestinal
Bleeding"` - makes only the quoted part mandatory.

### Special symbols

- Always treated as word separators: `/ \ | ? ! , ; .`
  `"Pooh.eats?honey!"` is equivalent to `"Pooh eats honey"`.
- Ignored only when they stand alone as a word: `+ - ( ) : ^ [ ] { } ~ * ? | & ;`
  ``"Pooh ` eats raspberries - honey"`` is equivalent to `"Pooh eats honey"`, but
  `"Pooh'eats raspberries-honey"` is not.
- Results that contain the symbol are returned before those that do not.

Quoting makes a special character mandatory: `[hip]` matches `[hip] fracture risk`,
`(hip fracture risk` and `hip fracture risk` alike, whereas `"[hip]"` matches only
`[hip] fracture risk`.

### Fuzzy matching

Typos and near-spellings still match. **Strok Myocardi8 Infarctiin Gastrointestinal Bleedi**
finds the same concepts as the correctly spelled phrase, ranked by similarity.

---

## Tuning the search query

Append `debug=true` to a search URL - for example
`https://qaathena.odysseusinc.com/search-terms/terms?debug=true` - to get an input field for the
boost object, a score column in the results, and the generated Solr request plus score
breakdown in the browser console (F12). Nothing is printed while the request and score are
unchanged.

Field weights are supplied as a boost object, grouped by how the term was matched - `phrase`,
`exactTerm`, `notExactTerm`, `asteriskTermBoosts`, and the `single*` variants that apply when
the query is a single term:

```json
{
    "notExactTerm": {
        "conceptNameText": 500,
        "conceptCodeText": 500,
        "conceptSynonymNameText": 200,
        "querySymbols": 10,
        "conceptCodeTextFuzzy": 50,
        "conceptNameTextFuzzy": 50,
        "conceptSynonymNameFuzzy": 20,
        "querySymbolsFuzzy": 1
    },
    "asteriskTermBoosts": {
        "conceptSynonymName": 40000,
        "conceptNameCi": 25000,
        "conceptNameText": 8000,
        "conceptCodeText": 10000,
        "conceptName": 60000,
        "conceptSynonymNameText": 5000,
        "conceptSynonymNameCi": 20000,
        "conceptCodeCi": 30000,
        "conceptCode": 80000
    },
    "phrase": {
        "conceptSynonymName": 40000,
        "conceptNameCi": 1000,
        "conceptName": 60000,
        "domainIdCi": 100,
        "conceptSynonymNameCi": 500,
        "conceptCodeCi": 10000,
        "conceptCode": 80000,
        "conceptClassIdCi": 100,
        "conceptId": 100000,
        "vocabularyIdCi": 100
    },
    "singleNotExactTermBoosts": {
        "conceptCodeText": 500,
        "conceptCodeTextFuzzy": 50
    },
    "exactTerm": {
        "conceptSynonymName": 40000,
        "conceptNameCi": 1000,
        "conceptName": 60000,
        "conceptSynonymNameCi": 500,
        "conceptCodeCi": 10000,
        "conceptCode": 80000,
        "conceptId": 100000,
        "querySymbols": 10
    },
    "singleExactTermBoosts": {
        "conceptCodeCi": 10000,
        "conceptCode": 80000
    },
    "singleAsteriskTermBoosts": {
        "conceptCodeText": 10000,
        "conceptCodeCi": 30000,
        "conceptCode": 80000
    }
}
```

### Generated query

Every query produces a phrase clause OR'd with per-term clauses. For the query string
`aspirin`:

```sql
( -- phrase
    concept_code:aspirin^80000 OR
    concept_name:aspirin^60000 OR
    concept_synonym_name:aspirin^40000 OR
    concept_code_ci:aspirin^10000 OR
    concept_name_ci:aspirin^1000 OR
    concept_synonym_name_ci:aspirin^500 OR
    concept_class_id_ci:aspirin^100 OR
    domain_id_ci:aspirin^100 OR
    vocabulary_id_ci:aspirin^100
)
OR
( -- single notExactTerm
    concept_code_text:aspirin^40000 OR
    concept_code_text:aspirin~0.7^30000
)
OR
( -- notExactTerm
    concept_code_text:aspirin^50 OR
    concept_code_text:aspirin~0.7^40 OR
    concept_name_text:aspirin^50 OR
    concept_name_text:aspirin~0.7^40 OR
    concept_synonym_name_text:aspirin^25 OR
    query_wo_symbols:aspirin^10
)
```

The variations follow from the rules above rather than from anything new:

- **`"aspirin"`** - the `notExactTerm` clauses are replaced by `exactTerm` ones over the
  non-`_text` fields, so no stemming or fuzzy variants apply.
- **`"45957786"`** - an all-digit query adds `concept_id` at `^100000`.
- **`aspirin paracetamol`** - one `notExactTerm` group per word, OR'd together.
- **`aspirin "paracetamol"`** - the quoted word becomes an `exactTerm` group that is `AND`ed
  with the unquoted word's `notExactTerm` group, making the quoted term mandatory.
- **`aspirin* ibupro*`** - each asterisked term becomes an `asteriskTermBoosts` prefix group,
  and the groups are `AND`ed.

Turn on `debug=true` to see the exact query for any input.
