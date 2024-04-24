Feature: Import vocabularies to the historical DB

  Background:
#    Given "" user is logged in
    When user import vocabulary from the "vocabulary_20200519" schema
     And user import vocabulary from the "vocabulary_20200517" schema
     And user import vocabulary from the "vocabulary_20200515" schema
     And user import vocabulary from the "vocabulary_20200511" schema

  Scenario: Import several versions
    When user inspects list of vocabulary release version
    And it is a list containing:
      | id       |
      | 20200519 |
      | 20200517 |
      | 20200515 |
      | 20200511 |
    When user generates a 20200519 version bundle
    And it is a list containing:
      | name                 | ext | rows |
      | CONCEPT_ANCESTOR     | csv | 0    |
      | CONCEPT_RELATIONSHIP | csv | 0    |
      | CONCEPT_SYNONYM      | csv | 0    |
      | DRUG_STRENGTH        | csv | 0    |
      | RELATIONSHIP         | csv | 0    |
      | VOCABULARY           | csv | 45   |
      | CONCEPT_CLASS        | csv | 0    |
      | CONCEPT              | csv | 0    |
      | DOMAIN               | csv | 0    |
    When user generates a 20200517 version bundle
#    check why concept is 184 and not 185 as it is in DB
    And it is a list containing:
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
    When user generates a 20200515 version bundle
    And it is a list containing:
      | name                 | ext | size | lines |
      | CONCEPT_ANCESTOR     | csv | 92   | 1     |
      | CONCEPT_RELATIONSHIP | csv | 89   | 1     |
      | CONCEPT_SYNONYM      | csv | 52   | 1     |
      | DRUG_STRENGTH        | csv | 218  | 1     |
      | RELATIONSHIP         | csv | 115  | 1     |
      | VOCABULARY           | csv | 2721 | 34    |
      | CONCEPT_CLASS        | csv | 61   | 1     |
      | CONCEPT              | csv | 142  | 1     |
      | DOMAIN               | csv | 40   | 1     |
    When user generates a 20200511 version bundle
    And it is a list containing:
      | name                 | ext | size | lines |
      | CONCEPT_ANCESTOR     | csv | 92   | 1     |
      | CONCEPT_RELATIONSHIP | csv | 89   | 1     |
      | CONCEPT_SYNONYM      | csv | 52   | 1     |
      | DRUG_STRENGTH        | csv | 218  | 1     |
      | RELATIONSHIP         | csv | 115  | 1     |
      | VOCABULARY           | csv | 2721 | 34    |
      | CONCEPT_CLASS        | csv | 61   | 1     |
      | CONCEPT              | csv | 142  | 1     |
      | DOMAIN               | csv | 40   | 1     |
#    And Delta delta cache for "" version is availabe
#
#  Scenario: Add new version
#    Given  user import vocabulary from the "test0" schema
#    Then it is list contains:
#      ||
#    When user generate bundle it contain follown files, with follonw amount of the records
#      |CONCEPT.csv|23|
#    And delta cache for "" version is availabe
#    And delta cache was rebuild
#
#  Scenario: Add old version
#    Given  user import vocabulary from the "test5" schema
#    Then it is list contains:
#      ||
#    When user generate bundle for "" version it contain follown files(with records amounts)
#      |CONCEPT.csv|23|
#    And delta cache for "" version is availabe
#    And delta cache was not rebuild
#
#
#  Scenario: Reimport latest version
#    Given user import vocabulary from the "test1-copy" schema
#    When user inspects list of vocabulary release version
#    Then it is list contains:
#      ||
#    When user generate bundle it contain follown files, with follonw amount of the records
#      |CONCEPT.csv|23|
#    And delta cache for "" version is availabe
#    And delta cache was rebuild
#
#
#  Scenario: Reimport second and third to last versions(That versions are using in the delta delta cache)
#    Then it is list contains:
#      ||
#      ||
#      ||
#    And delta cache for "" version is availabe
#
#    Given user import vocabulary from the "test4" schema
#    Then it is list contains:
#      ||
#      ||
#      ||
#      ||
#    And delta cache for "" version is availabe
#    And delta cache was not rebuild
#
#    Given user import vocabulary from the "test1-copy" schema
#    Then it is list contains:
#      ||
#      ||
#      ||
#      ||
#    And delta cache for "" version is availabe
#    And delta cache was rebuild