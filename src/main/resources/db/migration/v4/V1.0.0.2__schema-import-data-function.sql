CREATE OR REPLACE FUNCTION import_tables (
)
  RETURNS void AS
$body$
BEGIN
  --drop CONSTRAINTS

  --drop INDEXES
  DROP INDEX concept_relationship_c_1;
  DROP INDEX concept_relationship_c_2;

  --load
  TRUNCATE TABLE concept; ALTER TABLE concept SET UNLOGGED;
  COPY concept FROM '/home/athena_vocab_loader/v4_concept.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE concept_relationship; ALTER TABLE concept_relationship SET UNLOGGED;
  COPY concept_relationship FROM '/home/athena_vocab_loader/v4_concept_relationship.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE concept_synonym; ALTER TABLE concept_synonym SET UNLOGGED;
  COPY concept_synonym FROM '/home/athena_vocab_loader/v4_concept_synonym.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE concept_ancestor; ALTER TABLE concept_ancestor SET UNLOGGED;
  COPY concept_ancestor FROM '/home/athena_vocab_loader/v4_concept_ancestor.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE relationship;
  COPY relationship FROM '/home/athena_vocab_loader/v4_relationship.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE vocabulary;
  COPY vocabulary FROM '/home/athena_vocab_loader/v4_vocabulary.csv' delimiter ',' csv HEADER;

  TRUNCATE TABLE source_to_concept_map;
  COPY source_to_concept_map FROM '/home/athena_vocab_loader/v4_source_to_concept_map.csv' delimiter ',' csv HEADER;

  --enable CONSTRAINTS

  --restore INDEXES
  CREATE INDEX CONCEPT_RELATIONSHIP_C_1 ON CONCEPT_RELATIONSHIP (CONCEPT_ID_1);
  CREATE INDEX CONCEPT_RELATIONSHIP_C_2 ON CONCEPT_RELATIONSHIP (CONCEPT_ID_2);

  --return SET LOGGED
  ALTER TABLE concept SET LOGGED;
  ALTER TABLE concept_relationship SET LOGGED;
  ALTER TABLE concept_synonym SET LOGGED;
  ALTER TABLE concept_ancestor SET LOGGED;

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;