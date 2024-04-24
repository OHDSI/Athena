Feature: TODO DEV 

  Background:
    Given "" user is logged in
    And user import vocabulary from the "test1" schema
    And user import vocabulary from the "test2" schema
    And user import vocabulary from the "test3" schema
    And user import vocabulary from the "test4" schema

  Scenario: Generate delta bundle between last and first records
    When user generate delta bundle for "" version,  it contains files:
      | file name   | record amount |
      | CONCEPT.csv | 23            |
    And execute "delta.sql" script on the "" schema
    Then "" and "" schemas should be equal

  Scenario: Generate delta bundle between last and second to last  records
    When user generate delta bundle for "" version,  it contains files:
      | file name   | record amount |
      | CONCEPT.csv | 23            |
    And execute "delta.sql" script on the "" schema
    Then "" and "" schemas should be equal

  Scenario: Generate delta bundle between last and third to last  records
    When user generate delta bundle for "" version,  it contains files:
      | file name   | record amount |
      | CONCEPT.csv | 23            |
    And execute "delta.sql" script on the "" schema
    Then "" and "" schemas should be equal        
    
    