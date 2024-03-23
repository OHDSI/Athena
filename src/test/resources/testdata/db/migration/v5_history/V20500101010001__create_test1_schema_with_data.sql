-- create schema
CREATE SCHEMA test_1;

-- copy data from the test schema
CREATE TABLE test_1.concept AS              SELECT * FROM test.concept;
CREATE TABLE test_1.concept_ancestor AS     SELECT * FROM test.concept_ancestor;
CREATE TABLE test_1.concept_class AS        SELECT * FROM test.concept_class;
CREATE TABLE test_1.concept_relationship AS SELECT * FROM test.concept_relationship;
CREATE TABLE test_1.concept_synonym AS      SELECT * FROM test.concept_synonym;
CREATE TABLE test_1.domain AS               SELECT * FROM test.domain;
CREATE TABLE test_1.drug_strength AS        SELECT * FROM test.drug_strength;
CREATE TABLE test_1.relationship AS         SELECT * FROM test.relationship;
CREATE TABLE test_1.vocabulary AS           SELECT * FROM test.vocabulary;


-- change some of the data
update test_1.vocabulary set vocabulary_version = 'v5.0 01-MAY-99' where vocabulary_id = 'None';
DELETE FROM test_1.concept_synonym WHERE concept_id = 4161028;
DELETE FROM test_1.concept_relationship WHERE concept_id_1 = 4161028 OR concept_id_2 = 4161028;
DELETE FROM test_1.concept_ancestor WHERE ancestor_concept_id = 4161028 OR descendant_concept_id = 4161028;
DELETE FROM test_1.drug_strength WHERE drug_concept_id = 45547509;
DELETE FROM test_1.concept WHERE concept_id = 4161028;
DELETE FROM test_1.concept_class WHERE concept_class_id = 'Blood Pressure Pos';
DELETE FROM test_1.domain WHERE domain_id = 'Device';
DELETE FROM test_1.relationship where relationship_id in ( 'Using subst', 'Subst used by');
DELETE from test_1.vocabulary where vocabulary_id = 'ABMS';