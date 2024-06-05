# The cache saves the differences:
# - Between the last version and the second-to-last version.
# - Between the last version and the third-to-last version.
Feature: Test delta cache(It contains delta for the latest Vocabulary Release Versions)

  Background:
#    Given "" user is logged in
    When user import vocabulary from the "vocabulary_20200511" schema
    And user import vocabulary from the "vocabulary_20200513" schema
    And user import vocabulary from the "vocabulary_20200515" schema


  Scenario: Each new version refreshes the cache
    When user inspects list of vocabulary release version
    Then it is a list containing:
      | id       | cachedDate           |
      | 20200511 | ~(?<date20200511>.+) |
      | 20200513 | ~(?<date20200513>.+) |
      | 20200515 | ~(?<date20200515>.+) |
    And Date "date20200515" > "date20200513"
    And  Date "date20200513" > "date20200511"

  Scenario: Adding an older version does not affect the cache
    When user inspects list of vocabulary release version
    And it is a list containing:
      | id       | cachedDate           |
      | 20200511 | ~(?<date20200511>.+) |
      | 20200513 | ~(?<date20200513>.+) |
      | 20200515 | ~(?<date20200515>.+) |

    When user import vocabulary from the "vocabulary_20200509" schema
    And user inspects list of vocabulary release version
    Then it is a list containing:
      | id       | cachedDate             |
      | 20200509 |                        |
      | 20200511 | ~(?<date20200511x2>.+) |
      | 20200513 | ~(?<date20200513x2>.+) |
      | 20200515 | ~(?<date20200515x2>.+) |
    Then Date "date20200515x2" = "date20200515"
    And  Date "date20200513x2" = "date20200513"
    And  Date "date20200511x2" = "date20200511"

  Scenario: Adding a newer version refreshes the cache
    When user inspects list of vocabulary release version
    And it is a list containing:
      | id       | cachedDate           |
      | 20200511 | ~(?<date20200511>.+) |
      | 20200513 | ~(?<date20200513>.+) |
      | 20200515 | ~(?<date20200515>.+) |

    When user import vocabulary from the "vocabulary_20200517" schema
    And user inspects list of vocabulary release version
    Then it is a list containing:
      | id       | cachedDate             |
      | 20200511 | ~(?<date20200511x2>.+) |
      | 20200513 | ~(?<date20200513x2>.+) |
      | 20200515 | ~(?<date20200515x2>.+) |
      | 20200517 | ~(?<date20200517x2>.+) |
    And Date "date20200515x2" = "date20200515"
    And Date "date20200513x2" = "date20200513"
    And Date "date20200511x2" = "date20200511"
    And Date "date20200517x2" > "date20200515"

  Scenario: Reimporting the same version refreshes the cache
    When user inspects list of vocabulary release version
    Then it is a list containing:
      | id       | cachedDate           |
      | 20200511 | ~(?<date20200511>.+) |
      | 20200513 | ~(?<date20200513>.+) |
      | 20200515 | ~(?<date20200515>.+) |

    When user import vocabulary from the "vocabulary_20200515" schema
    Then user inspects list of vocabulary release version
    And it is a list containing:
      | id       | cachedDate             |
      | 20200511 | ~(?<date20200511x2>.+) |
      | 20200513 | ~(?<date20200513x2>.+) |
      | 20200515 | ~(?<date20200515x2>.+) |
    And Date "date20200515x2" > "date20200515"
    And Date "date20200513x2" = "date20200513"
    And Date "date20200511x2" = "date20200511"