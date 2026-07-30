Feature: Generate vocabulary download bundle for current version

  Background:
    Given user import vocabulary from the "vocabulary_20200513" schema

  Scenario: Download bundles by Current Release Version and compare it with the same historical version
    When user get vocabulary release version
    Then it is "v20200519"

    When user generates a bundle for current version
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
      | DOMAIN               | csv | 48   |
    When user compare with a 20200513 version bundle
    Then it is a list containing:
      | name                 | diff |
      | CONCEPT_ANCESTOR     | 0    |
      | CONCEPT_RELATIONSHIP | 0    |
      | CONCEPT_SYNONYM      | 0    |
      | DRUG_STRENGTH        | 0    |
      | RELATIONSHIP         | 0    |
      | VOCABULARY           | 0    |
      | CONCEPT_CLASS        | 0    |
      | CONCEPT              | 0    |
      | DOMAIN               | 0    |