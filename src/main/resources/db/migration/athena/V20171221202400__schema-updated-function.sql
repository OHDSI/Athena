CREATE OR REPLACE FUNCTION safe_import_of_tables()
  RETURNS VOID AS
$body$
DECLARE
  db VARCHAR := 'athena_db';
BEGIN

  RAISE NOTICE '%: safe_import_of_tables is started: %', db, now();

  PERFORM cancel_queries_except_current(db);

  LOCK TABLE VOCABULARY_CONVERSION IN ACCESS EXCLUSIVE MODE;

  PERFORM log_count(db, 'vocabulary_conversion');

  PERFORM import_tables();
  RAISE NOTICE '%: safe_import_of_tables() is finished', db;

  PERFORM log_count(db, 'vocabulary_conversion');

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;