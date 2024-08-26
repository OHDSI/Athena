Feature: Copy of vocabulary download bundle

  Background:

  Scenario: Copy and generate bundle
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
    And user inspect "VOCABULARY" file with "vocabulary_id" == "None"
    Then it is a list containing:
      | vocabulary_id | vocabulary_version |
      | None          | v5.0 19-MAY-20     |
    When user inspects list of bundles
    Then it is a list containing:
      | id               | name           | vocabularyReleaseVersion |
      | ~(?<bundleId>.+) | Bundle-current | v20200519                |


    When user set new release version: "v5.0 25-MAY-20"
    And user copy "bundleId" bundle with "Bundle-copy" and generate it
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
    And user inspect "VOCABULARY" file with "vocabulary_id" == "None"
    Then it is a list containing:
      | vocabulary_id | vocabulary_version |
      | None          | v5.0 25-MAY-20     |
    When user inspects list of bundles
    Then it is a list containing:
      | id    | name           | vocabularyReleaseVersion |
      | ~(.+) | Bundle-current | v20200519                |
      | ~(.+) | Bundle-copy    | v20200525                |
