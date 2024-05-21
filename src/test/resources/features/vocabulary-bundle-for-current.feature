Feature: Bundle for the concrete version

  Background:

  Scenario: copy and generate vocabulary bundle

    When user generates a bundle for current version
    Then it is a list containing:
      | name                 | ext | rows |
      | CONCEPT_ANCESTOR     | csv | 323  |
      | CONCEPT_RELATIONSHIP | csv | 674  |
      | CONCEPT_SYNONYM      | csv | 26   |
      | DRUG_STRENGTH        | csv | 6    |
      | RELATIONSHIP         | csv | 12   |
      | VOCABULARY           | csv | 45   |
      | CONCEPT_CLASS        | csv | 391  |
      | CONCEPT              | csv | 184  |
      | DOMAIN               | csv | 48   |
    When user inspects list of bundles
    Then it is a list containing:
      | id    | name           |
      | ~(.+) | Bundle-current |