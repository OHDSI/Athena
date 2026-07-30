Feature: Generate vocabulary download bundle by version

  Background:
#    Given "" user is logged in
    When user import vocabulary from the "vocabulary_20200511" schema
    And user import vocabulary from the "vocabulary_20200513" schema
    And user import vocabulary from the "vocabulary_20200515" schema


  Scenario: Download bundles by Vocabulary Release Version
    When user inspects list of vocabulary release version
    Then it is a list containing:
      | id       |
      | 20200511 |
      | 20200513 |
      | 20200515 |
    When user generates a 20200511 version bundle
    Then it is a list containing:
      | name                 | ext | rows |
      | CONCEPT_ANCESTOR     | csv | 319  |
      | CONCEPT_RELATIONSHIP | csv | 662  |
      | CONCEPT_SYNONYM      | csv | 25   |
      | DRUG_STRENGTH        | csv | 5    |
      | RELATIONSHIP         | csv | 10   |
      | VOCABULARY           | csv | 45   |
      | CONCEPT_CLASS        | csv | 390  |
      | CONCEPT              | csv | 183  |
      | CONCEPT_CPT4         | csv | 2    |
      | DOMAIN               | csv | 47   |

    When user generates a 20200513 version bundle
    Then it is a list containing:
      | name                 | ext | rows |
      | CONCEPT_ANCESTOR     | csv | 323  |
      | CONCEPT_RELATIONSHIP | csv | 674  |
      | CONCEPT_SYNONYM      | csv | 26   |
      | DRUG_STRENGTH        | csv | 6    |
      | RELATIONSHIP         | csv | 12   |
      | VOCABULARY           | csv | 46   |
      | CONCEPT_CLASS        | csv | 391  |
      | CONCEPT              | csv | 184  |
      | CONCEPT_CPT4         | csv | 3    |
      | DOMAIN               | csv | 48   |

    When user generates a 20200515 version bundle
    Then it is a list containing:
      | name                 | ext | rows |
      | CONCEPT_ANCESTOR     | csv | 319  |
      | CONCEPT_RELATIONSHIP | csv | 666  |
      | CONCEPT_SYNONYM      | csv | 24   |
      | DRUG_STRENGTH        | csv | 5    |
      | RELATIONSHIP         | csv | 10   |
      | VOCABULARY           | csv | 45   |
      | CONCEPT_CLASS        | csv | 390  |
      | CONCEPT              | csv | 183  |
      | DOMAIN               | csv | 47   |
