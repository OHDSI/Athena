-- create schema
CREATE SCHEMA test_2;

-- copy data from the test schema
CREATE TABLE test_2.concept AS              SELECT * FROM test.concept;
CREATE TABLE test_2.concept_ancestor AS     SELECT * FROM test.concept_ancestor;
CREATE TABLE test_2.concept_class AS        SELECT * FROM test.concept_class;
CREATE TABLE test_2.concept_relationship AS SELECT * FROM test.concept_relationship;
CREATE TABLE test_2.concept_synonym AS      SELECT * FROM test.concept_synonym;
CREATE TABLE test_2.domain AS               SELECT * FROM test.domain;
CREATE TABLE test_2.drug_strength AS        SELECT * FROM test.drug_strength;
CREATE TABLE test_2.relationship AS         SELECT * FROM test.relationship;
CREATE TABLE test_2.vocabulary AS           SELECT * FROM test.vocabulary;

-- test 2 40488901
update test_2.vocabulary set vocabulary_version = 'v5.0 02-MAY-99' where vocabulary_id = 'None';
DELETE FROM test_2.concept_synonym WHERE concept_id = 40488901;
DELETE FROM test_2.concept_relationship WHERE concept_id_1 = 40488901 OR concept_id_2 = 40488901;
DELETE FROM test_2.concept_ancestor WHERE ancestor_concept_id = 40488901 OR descendant_concept_id = 40488901;
DELETE FROM test_2.drug_strength WHERE drug_concept_id = 45910570;
DELETE FROM test_2.concept WHERE concept_id = 40488901;
DELETE FROM test_2.concept_class WHERE concept_class_id = 'Body Structure';
DELETE FROM test_2.domain WHERE domain_id = 'Device/Drug';
DELETE FROM test_2.relationship where relationship_id in ('Has proc context', 'Proc context of');
DELETE from test_2.vocabulary where vocabulary_id = 'AMT';


-- test 2  update
UPDATE test_2.concept SET concept_name = 'Updated Concept Name', valid_start_date = '2011-08-30' WHERE concept_id = 40488897;
UPDATE test_2.concept_ancestor SET min_levels_of_separation = 3, max_levels_of_separation = 5 WHERE ancestor_concept_id = 200962 OR descendant_concept_id = 40488897;
UPDATE test_2.concept_class SET concept_class_name = 'Updated Answers' WHERE concept_class_id = 'Answer';
UPDATE test_2.concept_class SET concept_class_name = 'Updated Ambulatory Patient Classification' WHERE concept_class_id = 'APC';
UPDATE test_2.concept_relationship SET relationship_id = 'Mapped from' WHERE concept_id_1 = 40488897 AND concept_id_2 = 40486666;
UPDATE test_2.concept_relationship SET relationship_id = 'Maps to' WHERE concept_id_1 = 40486666 AND concept_id_2 = 40488897;
UPDATE test_2.concept_synonym SET concept_synonym_name = 'Updated Synonym Name'||concept_synonym_name  WHERE concept_id = 40488897;
UPDATE test_2.domain SET domain_name = 'Updated Device/Observation', domain_concept_id = 46 WHERE domain_id = 'Device/Obs';
UPDATE test_2.drug_strength SET denominator_value = 0.001, box_size = 5 WHERE drug_concept_id = 45930504;
UPDATE test_2.relationship SET is_hierarchical = '7', defines_ancestry = '7' WHERE relationship_id = 'Contained in';
UPDATE test_2.relationship SET is_hierarchical = '9', defines_ancestry = '9' WHERE relationship_id = 'Contains';
UPDATE test_2.vocabulary SET vocabulary_name = 'Updated WHO Anatomic Therapeutic Chemical Classification', vocabulary_reference = 'Updated FDB UK distribution package', vocabulary_version = 'Updated RXNORM 2018-08-12' WHERE vocabulary_id = 'ATC';

