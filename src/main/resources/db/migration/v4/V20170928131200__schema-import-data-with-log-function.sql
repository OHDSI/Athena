CREATE OR REPLACE FUNCTION import_tables(
)
  RETURNS VOID AS
$body$
DECLARE
  db         VARCHAR := 'athena_cdm_v4_5';
  csv_prefix VARCHAR := 'v4_';
BEGIN
  RAISE NOTICE '%: import_tables is started: %', db, now();

  DROP INDEX concept_relationship_c_1;
  DROP INDEX concept_relationship_c_2;

  PERFORM import_table(db, csv_prefix, 'concept');
  PERFORM import_table(db, csv_prefix, 'concept_relationship');
  PERFORM import_table(db, csv_prefix, 'concept_synonym');

  PERFORM import_table(db, csv_prefix, 'concept_ancestor');
  PERFORM import_table(db, csv_prefix, 'relationship');
  PERFORM import_table(db, csv_prefix, 'vocabulary');

  PERFORM import_table(db, csv_prefix, 'source_to_concept_map');
  PERFORM import_table(db, csv_prefix, 'drug_strength');

  RAISE NOTICE 'restore INDEXES';
  CREATE INDEX CONCEPT_RELATIONSHIP_C_1
    ON CONCEPT_RELATIONSHIP (CONCEPT_ID_1);
  CREATE INDEX CONCEPT_RELATIONSHIP_C_2
    ON CONCEPT_RELATIONSHIP (CONCEPT_ID_2);

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;