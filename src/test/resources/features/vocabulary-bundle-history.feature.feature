Feature: TODO DEV

  Background:
    Given "" user is logged in
    And user import vocabulary from the "test1" schema
    And user import vocabulary from the "test2" schema
    And user import vocabulary from the "test3" schema
    And user import vocabulary from the "test4" schema

  Scenario: Generate bundle to the second to last version
    When user generate delta bundle for "" version,  it contains files:
      | file name   | record amount |
      | CONCEPT.csv | 23            |


  Scenario: Generate bundle to the first version
    When user generate delta bundle for "" version,  it contains files:
      | file name   | record amount |
      | CONCEPT.csv | 23            |
