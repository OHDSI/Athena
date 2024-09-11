-- 20200509 version before the original and it is copy of the 20200511, created only to check cache functionality
-- 20200517 version after the original  and it is copy of the 20200515, created only to check cache functionality

-- 20200511 version before the original version
SELECT copy_vocabulary_schema('vocabulary_testdata', 'vocabulary_20200511');
-- 20200513 version as a copy of the original version
SELECT copy_vocabulary_schema('vocabulary_testdata', 'vocabulary_20200513');
-- 20200515 version after the original version
SELECT copy_vocabulary_schema('vocabulary_testdata', 'vocabulary_20200515');



-- vocabulary_20200511
update vocabulary_20200511.vocabulary set vocabulary_version = 'v5.0 11-MAY-20' where vocabulary_id = 'None';
DELETE FROM vocabulary_20200511.concept_synonym WHERE concept_id = 4161028;
DELETE FROM vocabulary_20200511.concept_relationship WHERE concept_id_1 = 4161028 OR concept_id_2 = 4161028;
DELETE FROM vocabulary_20200511.concept_ancestor WHERE ancestor_concept_id = 4161028 OR descendant_concept_id = 4161028;
DELETE FROM vocabulary_20200511.drug_strength WHERE drug_concept_id = 45547509;
DELETE FROM vocabulary_20200511.concept WHERE concept_id = 4161028;
DELETE FROM vocabulary_20200511.concept WHERE concept_id = 42628634;
DELETE FROM vocabulary_20200511.concept_class WHERE concept_class_id = 'Blood Pressure Pos';
DELETE FROM vocabulary_20200511.domain WHERE domain_id = 'Device';
DELETE FROM vocabulary_20200511.relationship where relationship_id in ( 'Using subst', 'Subst used by');
DELETE from vocabulary_20200511.vocabulary where vocabulary_id = 'ABMS';

-- vocabulary_20200513
update vocabulary_20200513.vocabulary set vocabulary_version = 'v5.0 13-MAY-20' where vocabulary_id = 'None';

-- vocabulary_20200515
update vocabulary_20200515.vocabulary set vocabulary_version = 'v5.0 15-MAY-20' where vocabulary_id = 'None';
DELETE FROM vocabulary_20200515.concept_synonym WHERE concept_id = 40488901;
DELETE FROM vocabulary_20200515.concept_relationship WHERE concept_id_1 = 40488901 OR concept_id_2 = 40488901;
DELETE FROM vocabulary_20200515.concept_ancestor WHERE ancestor_concept_id = 40488901 OR descendant_concept_id = 40488901;
DELETE FROM vocabulary_20200515.drug_strength WHERE drug_concept_id = 45910570;
DELETE FROM vocabulary_20200515.concept WHERE concept_id = 40488901;
DELETE FROM vocabulary_20200515.concept WHERE concept_id = 2212194;
DELETE FROM vocabulary_20200515.concept_class WHERE concept_class_id = 'Body Structure';
DELETE FROM vocabulary_20200515.domain WHERE domain_id = 'Device/Drug';
DELETE FROM vocabulary_20200515.relationship where relationship_id in ('Has proc context', 'Proc context of');
DELETE from vocabulary_20200515.vocabulary where vocabulary_id = 'AMT';

UPDATE vocabulary_20200515.concept SET concept_name = 'Updated Concept Name', valid_start_date = '2011-08-30' WHERE concept_id = 40488897;
UPDATE vocabulary_20200515.concept SET concept_name = 'Updated Aldolase', valid_start_date = '2009-08-30' WHERE concept_id = 2212193;
UPDATE vocabulary_20200515.concept_ancestor SET min_levels_of_separation = 3, max_levels_of_separation = 5 WHERE ancestor_concept_id = 200962 OR descendant_concept_id = 40488897;
UPDATE vocabulary_20200515.concept_class SET concept_class_name = 'Updated Answers' WHERE concept_class_id = 'Answer';
UPDATE vocabulary_20200515.concept_class SET concept_class_name = 'Updated Ambulatory Patient Classification' WHERE concept_class_id = 'APC';
UPDATE vocabulary_20200515.concept_relationship SET relationship_id = 'Mapped from' WHERE concept_id_1 = 40488897 AND concept_id_2 = 40486666;
UPDATE vocabulary_20200515.concept_relationship SET relationship_id = 'Maps to' WHERE concept_id_1 = 40486666 AND concept_id_2 = 40488897;
UPDATE vocabulary_20200515.concept_synonym SET concept_synonym_name = 'Updated Synonym Name'||concept_synonym_name  WHERE concept_id = 40488897;
UPDATE vocabulary_20200515.domain SET domain_name = 'Updated Device/Observation', domain_concept_id = 46 WHERE domain_id = 'Device/Obs';
UPDATE vocabulary_20200515.drug_strength SET denominator_value = 0.001, box_size = 5 WHERE drug_concept_id = 45930504;
UPDATE vocabulary_20200515.relationship SET is_hierarchical = '7', defines_ancestry = '7' WHERE relationship_id = 'Contained in';
UPDATE vocabulary_20200515.relationship SET is_hierarchical = '9', defines_ancestry = '9' WHERE relationship_id = 'Contains';
UPDATE vocabulary_20200515.vocabulary SET vocabulary_name = 'Updated WHO Anatomic Therapeutic Chemical Classification', vocabulary_reference = 'Updated FDB UK distribution package', vocabulary_version = 'Updated RXNORM 2018-08-12' WHERE vocabulary_id = 'ATC';


-- vocabulary_20200509
SELECT copy_vocabulary_schema('vocabulary_20200511', 'vocabulary_20200509');
update vocabulary_20200509.vocabulary set vocabulary_version = 'v5.0 09-MAY-20' where vocabulary_id = 'None';
-- vocabulary_20200517
SELECT copy_vocabulary_schema('vocabulary_20200515', 'vocabulary_20200517');
update vocabulary_20200517.vocabulary set vocabulary_version = 'v5.0 17-MAY-20' where vocabulary_id = 'None';