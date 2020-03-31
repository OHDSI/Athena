# 0 Customize query

## Boosts object
We use an object with boosts in order to configure the solr search query:
```json
{
  "notExactTerm": {
    "conceptCodeText": 100,
    "conceptCodeTextFuzzy": 100,
    "conceptNameText": 50,
    "conceptNameTextFuzzy": 50,
    "conceptSynonymName": 25
  },
  "exactTerm": {
    "id": 100000,
    "conceptCode": 10000,
    "conceptName": 1000,
    "conceptSynonymName": 500,
    "querySymbols": 1
  },
  "exactPhrase": {
    "id": 100000,
    "conceptCodeCi": 80000,
    "conceptNameCi": 60000,
    "conceptSynonymNameCi": 40000,
    "conceptCode": 10000,
    "conceptName": 1000,
    "conceptSynonymName": 500,
    "conceptClassId": 100,
    "domainId": 100,
    "vocabularyId": 100,
    "standardConcept": 100,
    "invalidReason": 100
  }
}
```
## Query examples 
examples of generated solr queries:

**query string** : aspirin
```sql
(  --exactPhrase
     id:aspirin^100000 OR
     concept_code_ci:aspirin^80000 OR
     concept_name_ci:aspirin^60000 OR
     concept_synonym_name_ci:aspirin^40000 OR
     concept_code:aspirin^10000 OR
     concept_name:aspirin^1000 OR
     concept_synonym_name:aspirin^500 OR
     concept_class_id:aspirin^100 OR
     domain_id:aspirin^100 OR
     vocabulary_id:aspirin^100 OR
     standard_concept:aspirin^100 OR
     invalid_reason:aspirin^100
 ) OR
 (
    ( --notExactTerm
         concept_code_text:aspirin^100 OR
         concept_code_text:aspirin~0.7^100 OR
         concept_name_text:aspirin^500 OR
         concept_name_text:aspirin~0.7^50 OR
         concept_synonym_name_text:aspirin^25
     )
 )

```
**query string**: "aspirin"
```sql
 (  --exactPhrase
      id:aspirin^100000 OR
      concept_code_ci:aspirin^80000 OR
      concept_name_ci:aspirin^60000 OR
      concept_synonym_name_ci:aspirin^40000 OR
      concept_code:aspirin^10000 OR
      concept_name:aspirin^1000 OR
      concept_synonym_name:aspirin^500 OR
      concept_class_id:aspirin^100 OR
      domain_id:aspirin^100 OR
      vocabulary_id:aspirin^100 OR
      standard_concept:aspirin^100 OR
      invalid_reason:aspirin^100
  ) OR
  (
      (  -- exactTerm
          id:"aspirin"^100000 OR
          concept_code:"aspirin"^10000 OR
          concept_name:"aspirin"^1000 OR
          concept_synonym_name:"aspirin"^500 OR
          query:"aspirin"^1
      )
  )
```

**query string**: aspirin paracetamol
```sql
  (  --exactPhrase
       id:aspirin\ paracetamol^100000 OR
       concept_code_ci:aspirin\ paracetamol^80000 OR
       concept_name_ci:aspirin\ paracetamol^60000 OR
       concept_synonym_name_ci:aspirin\ paracetamol^40000 OR
       concept_code:aspirin\ paracetamol^10000 OR
       concept_name:aspirin\ paracetamol^1000 OR
       concept_synonym_name:aspirin\ paracetamol^500 OR
       concept_class_id:aspirin\ paracetamol^100 OR
       domain_id:aspirin\ paracetamol^100 OR
       vocabulary_id:aspirin\ paracetamol^100 OR
       standard_concept:aspirin\ paracetamol^100 OR
       invalid_reason:aspirin\ paracetamol^100
   ) OR
   (
       ( --notExactTerm
           concept_code_text:aspirin^100 OR
           concept_code_text:aspirin~0.7^100 OR
           concept_name_text:aspirin^50 OR
           concept_name_text:aspirin~0.7^50 OR
           concept_synonym_name_text:aspirin^25
       ) OR
       ( --notExactTerm
           concept_code_text:paracetamol^100 OR
           concept_code_text:paracetamol~0.7^100 OR
           concept_name_text:paracetamol^50 OR
           concept_name_text:paracetamol~0.7^50 OR
           concept_synonym_name_text:paracetamol^25
       )
   )
      
```
**query string**: aspirin "paracetamol"
```sql
( --exactPhrase
    id:aspirin\ paracetamol^100000 OR
    concept_code_ci:aspirin\ paracetamol^80000 OR
    concept_name_ci:aspirin\ paracetamol^60000 OR
    concept_synonym_name_ci:aspirin\ paracetamol^40000 OR
    concept_code:aspirin\ paracetamol^10000 OR
    concept_name:aspirin\ paracetamol^1000 OR
    concept_synonym_name:aspirin\ paracetamol^500 OR
    concept_class_id:aspirin\ paracetamol^100 OR
    domain_id:aspirin\ paracetamol^100 OR
    vocabulary_id:aspirin\ paracetamol^100 OR
    standard_concept:aspirin\ paracetamol^100 OR
    invalid_reason:aspirin\ paracetamol^100
 ) OR
 (
     (
         (-- exactTerm
             id:"paracetamol"^100000 OR
             concept_code:"paracetamol"^10000 OR
             concept_name:"paracetamol"^1000 OR
             concept_synonym_name:"paracetamol"^500 OR
             query:"paracetamol"^1
         )
     )
     OR
     (
         (
             (-- exactTerm
                 id:"paracetamol"^100000 OR
                 concept_code:"paracetamol"^10000 OR
                 concept_name:"paracetamol"^1000 OR
                 concept_synonym_name:"paracetamol"^500 OR
                 query:"paracetamol"^1
             )
         )
          AND
          (
              (--notExactTerm
                concept_code_text:aspirin^100 OR
                concept_code_text:aspirin~0.7^100 OR
                concept_name_text:aspirin^50 OR
                concept_name_text:aspirin~0.7^50 OR
                concept_synonym_name_text:aspirin^25
             )
         )
     )
 )
```


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
