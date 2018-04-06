CREATE OR REPLACE FUNCTION import_tables (
)
  RETURNS void AS
$body$
BEGIN
  --drop CONSTRAINTS

  --drop INDEXES
  DROP INDEX DRUG_CONCEPTID;
  DROP INDEX INGREDIENT_CONCEPTID;
  DROP INDEX CONCEPT_RELATIONSHIP_C_1;
  DROP INDEX CONCEPT_RELATIONSHIP_C_2;
  DROP INDEX ancestor_conceptid;
  DROP INDEX descendant_conceptid;
  DROP INDEX concept_vocab;

  --load
  TRUNCATE TABLE concept; ALTER TABLE concept SET UNLOGGED;
  COPY concept FROM '/home/athena_vocab_loader/v5_concept.csv' delimiter ',' csv HEADER;
  ALTER TABLE concept SET LOGGED;

  TRUNCATE TABLE concept_relationship; ALTER TABLE concept_relationship SET UNLOGGED;
  COPY concept_relationship FROM '/home/athena_vocab_loader/v5_concept_relationship.csv' delimiter ',' csv HEADER;
  ALTER TABLE concept_relationship SET LOGGED;

  TRUNCATE TABLE concept_synonym; ALTER TABLE concept_synonym SET UNLOGGED;
  COPY concept_synonym FROM '/home/athena_vocab_loader/v5_concept_synonym.csv' delimiter ',' csv HEADER;
  ALTER TABLE concept_synonym SET LOGGED;

  TRUNCATE TABLE concept_ancestor; ALTER TABLE concept_ancestor SET UNLOGGED;
  COPY concept_ancestor FROM '/home/athena_vocab_loader/v5_concept_ancestor.csv' delimiter ',' csv HEADER;
  ALTER TABLE concept_ancestor SET LOGGED;

  TRUNCATE TABLE relationship;
  COPY relationship FROM '/home/athena_vocab_loader/v5_relationship.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE vocabulary;
  COPY vocabulary FROM '/home/athena_vocab_loader/v5_vocabulary.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE concept_class;
  COPY concept_class FROM '/home/athena_vocab_loader/v5_concept_class.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE domain;
  COPY domain FROM '/home/athena_vocab_loader/v5_domain.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE drug_strength;
  COPY drug_strength FROM '/home/athena_vocab_loader/v5_drug_strength.csv' delimiter ',' csv HEADER;

  --enable CONSTRAINTS

  --restore INDEXES
  CREATE INDEX DRUG_CONCEPTID ON DRUG_STRENGTH (DRUG_CONCEPT_ID);
  CREATE INDEX INGREDIENT_CONCEPTID ON DRUG_STRENGTH (INGREDIENT_CONCEPT_ID);

  CREATE INDEX CONCEPT_RELATIONSHIP_C_1 ON CONCEPT_RELATIONSHIP (CONCEPT_ID_1);
  CREATE INDEX CONCEPT_RELATIONSHIP_C_2 ON CONCEPT_RELATIONSHIP (CONCEPT_ID_2);

  CREATE INDEX ANCESTOR_CONCEPTID ON CONCEPT_ANCESTOR (ANCESTOR_CONCEPT_ID);
  CREATE INDEX DESCENDANT_CONCEPTID ON CONCEPT_ANCESTOR (DESCENDANT_CONCEPT_ID);

  CREATE INDEX CONCEPT_VOCAB ON CONCEPT (VOCABULARY_ID);

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;