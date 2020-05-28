# 1 Phrase search

Search provides the ability to search by phrase. All results are sorted by default according to the following criteria:

 -full phrase match 
- concepts contain all the words from the search phrase
- result based on two parameters, the number of searched words in the result and importance of each word (importance is calculated for each word, the words that are rearer among all documents are more important)

Example:

Search phrase: **Stroke Myocardial Infarction Gastrointestinal Bleeding**

Name | sort priority explanation |
---- | ---- |
Stroke Myocardial Infarction Gastrointestinal Bleeding| full match  |
Gastrointestinal Bleeding Myocardial Infarction Stroke| all words |
Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction| 3 words |
Stroke Myocardial Infarction Bleeding in Back| 2 words |
Bleeding in Back Gastrointestinal Bleeding| 2 word |
Stroke Myocardial Infarction| 2 word |
Stroke Myocardial Infarction Strok| 2 words |
Stroke Myocardial Infarction Stroke Nothin| 2 words |
Stroke Myocardial Infarction  Renal Dysfunction| 2 words |
Stroke Myocardial Infarction Renal Dysfunction and Nothing| 1 words |
stroke| 1 words |
Stroke| 1 words |
Strook| 1 words |


NB: the search goes through all concept fields, but the highest priority is given to CONCEPT_NAME and CONCEPT_CODE

# 2 Exact search

Using quotation marks forces an exact-match search. 

For an exact search, the following conditions are met
- the word must be present
- not case sensitive, the number of spaces between words does not matter
- stemming is disabled(the word/words must be present exactly as it is in quotation marks)

Example 1:

Search phrase: **"Stroke Myocardial Infarction Gastrointestinal Bleeding"**

Name |
--- | 
Stroke Myocardial Infarction Gastrointestinal Bleeding |
Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction |

Example 2:

Search phrase:  **"Stroke Myocardial Infarction "Gastrointestinal Bleeding"**

Name |
--- |
Stroke Myocardial Infarction Gastrointestinal Bleeding |
Gastrointestinal Bleeding Myocardial Infarction Stroke |
Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction |
Bleeding in Back Gastrointestinal Bleeding |

# 3 Special symbols

For special symbols, the following conditions are met
- These special symbols are always ignored and treated as words separation symbols: / \ | ? ! , ;   .
  e.g. "Pooh.eats?honey!" equals "Pooh eats honey" 
- All other special symbols ignored only if it is a separate word: + - ( ) : ^ [ ] { } ~ * ? | & ;
  e.g. "Pooh ` eats raspberries - honey" equals "Pooh eats honey", but "Pooh'eats raspberries-honey" will remain the same  
- the first funded result will be with characters and then without

Search phrase: **[hip]**

Name |
--- |
[hip] fracture risk |
[Hip] fracture risk |
[hip fracture risk |
hip] fracture risk |
(hip fracture risk |
(hip) fracture risk |
hip fracture risk |
hip) fracture risk |
hip} fracture risk |
hip} fracture risk |
{hip fracture risk |


A special character becomes mandatory if the word is surrounded by quotation marks.

Search phrase:  **"[hip]"**

Name |
--- |
[hip] fracture risk | 
[Hip] fracture risk | 


# 4 Approximate matching (fuzzy searching)

In case of a typo, or if there is a similar spelling of the word, the most similar result will be found

Search phrase: **Strok Myocardi8 Infarctiin Gastrointestinal Bleedi**

Name |
--- | 
Gastrointestinal Bleeding Myocardial Infarction Stroke|
Stroke Myocardial Infarction Gastrointestinal Bleeding|
Stroke Myocardial Infarction  Gastrointestinal Bleeding and Renal Dysfunction|
Stroke Myocardial Infarction Strok|
Bleeding in Back Gastrointestinal Bleeding|
Stroke Myocardial Infarction Bleeding in Back|
Stroke Myocardial Infarction|
Stroke Myocardial Infarction Stroke Nothin|
Stroke Myocardial Infarction  Renal Dysfunction|
Stroke Myocardial Infarction Renal Dysfunction and Nothing|
stroke|
Stroke|
Stroo |


# 1 Customize query


## Activate customizing search query mode
You can activate this  mode by adding `debug=true` params in url
https://qaathena.odysseusinc.com/search-terms/terms?debug=true

* the text input field for the boost object will appear below the search input
* the score column will be appeared
* the generated solr requests and score calculation information will be printed to the browser console (to see it, open developer tools by F12). if the  solr-request/score has not changed then this info will not be printed


## Boosts object
We use an object with boosts in order to configure the solr search query:
```json
{
  "notExactTerm": {
    "conceptNameText": 50,
    "conceptCodeText": 50,
    "conceptSynonymNameText": 25,
    "conceptCodeTextFuzzy": 40,
    "queryWoSymbols": 10,
    "conceptNameTextFuzzy": 40
  },
  "singleNotExactTermBoosts": {
    "conceptCodeText": 40000,
    "conceptCodeTextFuzzy": 30000
  },
  "exactTerm": {
    "conceptSynonymName": 40000,
    "conceptNameCi": 1000,
    "conceptName": 60000,
    "conceptSynonymNameCi": 500,
    "conceptCodeCi": 10000,
    "conceptCode": 80000,
    "conceptId": 100000,
    "querySymbols": 1
  },
  "singleExactTermBoosts": {
    "conceptCodeCi": 10000,
    "conceptCode": 80000
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
  "singleAsteriskTermBoosts": {
    "conceptCodeText": 10000,
    "conceptCodeCi": 30000,
    "conceptCode": 80000
  },
  "phrase": {
    "conceptSynonymName": 40000,
    "conceptNameCi": 1000,
    "domainIdCi": 100,
    "conceptName": 60000,
    "conceptSynonymNameCi": 500,
    "conceptCodeCi": 10000,
    "conceptClassIdCi": 100,
    "conceptCode": 80000,
    "conceptId": 100000,
    "vocabularyIdCi": 100
  }
}
```
## Query examples 
examples of generated solr queries:

**query string** : aspirin
```sql
( --phrase
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
**query string**: "aspirin"
```sql
( --phrase
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
( --single exactTerm  
    concept_code:aspirin^80000 OR
    concept_code_ci:aspirin^10000
)
OR
( --exactTerm
    concept_code:"aspirin"^80000 OR
    concept_name:"aspirin"^60000 OR
    concept_synonym_name:"aspirin"^40000 OR
    concept_code_ci:"aspirin"^10000 OR
    concept_name_ci:"aspirin"^1000 OR
    concept_synonym_name_ci:"aspirin"^500 OR
    query:"aspirin"^1
)

```
**query string**: "45957786" (in case we are searching an exact number the field 'concept_id' is added)
```sql
( --phrase
    concept_code:45957786^80000 OR
    concept_name:45957786^60000 OR
    concept_synonym_name:45957786^40000 OR
    concept_code_ci:45957786^10000 OR
    concept_name_ci:45957786^1000 OR
    concept_synonym_name_ci:45957786^500 OR
    concept_class_id_ci:45957786^100 OR
    domain_id_ci:45957786^100 OR
    vocabulary_id_ci:45957786^100 OR
    concept_id:45957786^100000
) 
OR
( -- single exactTerm
    concept_code:45957786^80000 OR
    concept_code_ci:45957786^10000
) 
OR
(-- exactTerm
    concept_code:"45957786"^80000 OR
    concept_name:"45957786"^60000 OR
    concept_synonym_name:"45957786"^40000 OR
    concept_code_ci:"45957786"^10000 OR
    concept_name_ci:"45957786"^1000 OR
    concept_synonym_name_ci:"45957786"^500 OR
    query:"45957786"^1 OR
    concept_id:45957786^100000
)
```

**query string**: aspirin paracetamol
```sql
( --phrase
    concept_code:aspirin\ paracetamol^80000 OR
    concept_name:aspirin\ paracetamol^60000 OR
    concept_synonym_name:aspirin\ paracetamol^40000 OR
    concept_code_ci:aspirin\ paracetamol^10000 OR
    concept_name_ci:aspirin\ paracetamol^1000 OR
    concept_synonym_name_ci:aspirin\ paracetamol^500 OR
    concept_class_id_ci:aspirin\ paracetamol^100 OR
    domain_id_ci:aspirin\ paracetamol^100 OR
    vocabulary_id_ci:aspirin\ paracetamol^100
) 
OR
(
    ( --notExactTerm
        concept_code_text:aspirin^50 OR
        concept_code_text:aspirin~0.7^40 OR
        concept_name_text:aspirin^50 OR
        concept_name_text:aspirin~0.7^40 OR
        concept_synonym_name_text:aspirin^25 OR
        query_wo_symbols:aspirin^10
    ) 
    OR
    (--notExactTerm
        concept_code_text:paracetamol^50 OR
        concept_code_text:paracetamol~0.7^40 OR
        concept_name_text:paracetamol^50 OR
        concept_name_text:paracetamol~0.7^40 OR
        concept_synonym_name_text:paracetamol^25 OR
        query_wo_symbols:paracetamol^10
    )
)
```
**query string**: aspirin "paracetamol"
```sql
( --phrase
    concept_code:aspirin\ paracetamol^80000 OR
    concept_name:aspirin\ paracetamol^60000 OR
    concept_synonym_name:aspirin\ paracetamol^40000 OR
    concept_code_ci:aspirin\ paracetamol^10000 OR
    concept_name_ci:aspirin\ paracetamol^1000 OR
    concept_synonym_name_ci:aspirin\ paracetamol^500 OR
    concept_class_id_ci:aspirin\ paracetamol^100 OR
    domain_id_ci:aspirin\ paracetamol^100 OR
    vocabulary_id_ci:aspirin\ paracetamol^100
) 
OR
(
    (  -- exactTerm
        concept_code:"paracetamol"^80000 OR
        concept_name:"paracetamol"^60000 OR
        concept_synonym_name:"paracetamol"^40000 OR
        concept_code_ci:"paracetamol"^10000 OR
        concept_name_ci:"paracetamol"^1000 OR
        concept_synonym_name_ci:"paracetamol"^500 OR
        query:"paracetamol"^1
    ) 
    OR
    (
        (  -- exactTerm
            concept_code:"paracetamol"^80000 OR
            concept_name:"paracetamol"^60000 OR
            concept_synonym_name:"paracetamol"^40000 OR
            concept_code_ci:"paracetamol"^10000 OR
            concept_name_ci:"paracetamol"^1000 OR
            concept_synonym_name_ci:"paracetamol"^500 OR
            query:"paracetamol"^1
        ) 
        AND 
        ( --notExactTerm
            concept_code_text:aspirin^50 OR
            concept_code_text:aspirin~0.7^40 OR
            concept_name_text:aspirin^50 OR
            concept_name_text:aspirin~0.7^40 OR
            concept_synonym_name_text:aspirin^25 OR
            query_wo_symbols:aspirin^10
        )
    )
)

```
Requirement for search with an asterisk:

**query string**: aspirin* ibupro*

**result**:
```sql   
( --phrase
    concept_code:aspirin\*\ ibupro\*^80000 OR
    concept_name:aspirin\*\ ibupro\*^60000 OR
    concept_synonym_name:aspirin\*\ ibupro\*^40000 OR
    concept_code_ci:aspirin\*\ ibupro\*^10000 OR
    concept_name_ci:aspirin\*\ ibupro\*^1000 OR
    concept_synonym_name_ci:aspirin\*\ ibupro\*^500 OR
    concept_class_id_ci:aspirin\*\ ibupro\*^100 OR
    domain_id_ci:aspirin\*\ ibupro\*^100 OR
    vocabulary_id_ci:aspirin\*\ ibupro\*^100
)
OR
(
    ( --asterisk
        concept_code:aspirin*^80000 OR
        concept_name:aspirin*^60000 OR
        concept_synonym_name:aspirin*^40000 OR
        concept_code_ci:aspirin*^30000 OR
        concept_name_ci:aspirin*^25000 OR
        concept_synonym_name_ci:aspirin*^20000 OR
        concept_code_text:aspirin*^10000 OR
        concept_name_text:aspirin*^8000 OR
        concept_synonym_name_text:aspirin*^5000
    )
    AND
    ( --asterisk
        concept_code:ibupro*^80000 OR
        concept_name:ibupro*^60000 OR
        concept_synonym_name:ibupro*^40000 OR
        concept_code_ci:ibupro*^30000 OR
        concept_name_ci:ibupro*^25000 OR
        concept_synonym_name_ci:ibupro*^20000 OR
        concept_code_text:ibupro*^10000 OR
        concept_name_text:ibupro*^8000 OR
        concept_synonym_name_text:ibupro*^5000
    )
)


```

