CREATE OR REPLACE FUNCTION safe_import_of_tables(
)
  RETURNS VOID AS
$body$
DECLARE
  db VARCHAR := 'athena_cdm_v4_5';
BEGIN

  RAISE NOTICE '%: safe_import_of_tables is started: %', db, now();

  PERFORM kill_sessions_except_current(db);

  RAISE NOTICE '%: lock tables is started', db;

  LOCK TABLE concept IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_relationship IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_synonym IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_ancestor IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE relationship IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE vocabulary IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE drug_strength IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE source_to_concept_map IN ACCESS EXCLUSIVE MODE;

  PERFORM import_tables();
  RAISE NOTICE '%: safe_import_of_tables() is finished', db;
END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;