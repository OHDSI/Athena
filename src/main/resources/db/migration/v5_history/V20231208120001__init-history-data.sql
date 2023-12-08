CREATE SCHEMA public_fwd;

CREATE EXTENSION postgres_fdw;
CREATE SERVER athena_cdm_v5_server FOREIGN DATA WRAPPER postgres_fdw OPTIONS (host 'localhost', dbname 'athena_cdm_v5');

ALTER SCHEMA public_fwd owner TO ohdsi;
IMPORT FOREIGN SCHEMA public FROM SERVER athena_cdm_v5_server INTO public_fwd;

-- Concepts
CREATE TABLE public.concept_history_20230831 PARTITION OF public.concept_history FOR VALUES IN (20230831);
INSERT INTO public.concept_history_20230831
SELECT c.*, 20230831 as version
FROM public_fwd.concept c;

-- Concept Ancestor
CREATE TABLE public.concept_ancestor_history_20230831 PARTITION OF public.concept_ancestor_history FOR VALUES IN (20230831);
INSERT INTO public.concept_ancestor_history_20230831
SELECT ca.*, a.VOCABULARY_ID AS ANCESTOR_VOCABULARY_ID, d.VOCABULARY_ID AS DESCENDANT_VOCABULARY_ID, 20230831 as version
FROM public_fwd.concept_ancestor AS ca
         JOIN public_fwd.CONCEPT AS a ON ca.ANCESTOR_CONCEPT_ID = a.CONCEPT_ID
         JOIN public_fwd.CONCEPT AS d ON ca.DESCENDANT_CONCEPT_ID = d.CONCEPT_ID;

-- Concept Class
CREATE TABLE public.concept_class_history_20230831 PARTITION OF public.concept_class_history FOR VALUES IN (20230831);
INSERT INTO public.concept_class_history_20230831
SELECT cc.*, 20230831 as version
FROM public_fwd.concept_class cc;

-- Concept Relationship
CREATE TABLE public.concept_relationship_history_20230831 PARTITION OF public.concept_relationship_history FOR VALUES IN (20230831);
INSERT INTO public.concept_relationship_history_20230831
SELECT cr.*, c1.VOCABULARY_ID AS VOCABULARY_ID_1, c2.VOCABULARY_ID AS VOCABULARY_ID_2, 20230831 AS VERSION
FROM public_fwd.concept_relationship AS cr
         JOIN public_fwd.CONCEPT AS c1 ON cr.CONCEPT_ID_1 = c1.CONCEPT_ID
         JOIN public_fwd.CONCEPT AS c2 ON cr.CONCEPT_ID_2 = c2.CONCEPT_ID
WHERE cr.relationship_id IN
      (SELECT r.relationship_id
       FROM public_fwd.relationship AS r
       WHERE r.relationship_id > r.reverse_relationship_id);

-- Concept Synonym
CREATE TABLE public.concept_synonym_history_20230831 PARTITION OF public.concept_synonym_history FOR VALUES IN (20230831);
INSERT INTO public.concept_synonym_history_20230831
SELECT cs.*, c.vocabulary_id, 20230831 as version
FROM public_fwd.concept_synonym AS cs
         JOIN public_fwd.concept AS c ON cs.CONCEPT_ID = c.CONCEPT_ID;

--Drug Strength
CREATE TABLE public.drug_strength_history_20230831 PARTITION OF public.drug_strength_history FOR VALUES IN (20230831);
INSERT INTO public.drug_strength_history_20230831
SELECT ds.*, c.vocabulary_id, 20230831 as version
FROM public_fwd.drug_strength as ds
         JOIN public_fwd.concept AS c ON ds.DRUG_CONCEPT_ID = c.CONCEPT_ID;

-- Concept Versions
CREATE TABLE public.concept_versions_history_20230831 PARTITION OF public.concept_versions_history FOR VALUES IN (20230831);
INSERT INTO public.concept_versions_history_20230831
SELECT cv.*, 20230831 as version
FROM public_fwd.concept_versions cv;

-- Domain
CREATE TABLE public.domain_history_20230831 PARTITION OF public.domain_history FOR VALUES IN (20230831);
INSERT INTO public.domain_history_20230831
SELECT d.*, 20230831 as version
FROM public_fwd.domain d;

-- Relationship History
CREATE TABLE public.relationship_history_20230831 PARTITION OF public.relationship_history FOR VALUES IN (20230831);
INSERT INTO public.relationship_history_20230831
SELECT r.*, 20230831 as version
FROM public_fwd.relationship r;

-- Create the Vocabulary History Table
CREATE TABLE public.vocabulary_history_20230831 PARTITION OF public.vocabulary_history FOR VALUES IN (20230831);
INSERT INTO public.vocabulary_history_20230831
SELECT v.*, 20230831 as version
FROM public_fwd.vocabulary v;

DROP SCHEMA public_fwd CASCADE;