CREATE OR REPLACE FUNCTION log_count(db VARCHAR, table_name VARCHAR)
  RETURNS VOID AS $$
DECLARE
  query VARCHAR;
  cnt   BIGINT;
BEGIN
  query := 'SELECT count(*) FROM ' || quote_ident($2);
  EXECUTE query
  INTO cnt;
  RAISE NOTICE '%: % has % records', db, table_name, cnt;
END;
$$ LANGUAGE plpgsql;
-------------------------------------
CREATE OR REPLACE FUNCTION import_table(db VARCHAR, csv_prefix VARCHAR, table_name VARCHAR)
  RETURNS VOID AS $$
DECLARE
  query VARCHAR;
BEGIN

  PERFORM log_count(db, table_name);
  query := 'TRUNCATE TABLE ' || quote_ident(table_name);
  EXECUTE query;

  query := ' ALTER TABLE  ' || quote_ident(table_name) || ' SET UNLOGGED';
  EXECUTE query;

  query :=
  'COPY ' || quote_ident(table_name) || ' FROM ''/home/athena_vocab_loader/' || csv_prefix || quote_ident(table_name) ||
  '.csv''' || ' delimiter '','' csv HEADER;';
  EXECUTE query;

  query := ' ALTER TABLE  ' || quote_ident(table_name) || ' SET LOGGED';
  EXECUTE query;

  PERFORM log_count(db, table_name);

END;
$$ LANGUAGE plpgsql;
-------------------------------------
CREATE OR REPLACE FUNCTION kill_sessions_except_current(db VARCHAR)
  RETURNS VOID AS
$body$
DECLARE
  terminated VARCHAR := '';
BEGIN

  SELECT pg_terminate_backend(pg_stat_activity.pid)
  INTO terminated
  FROM pg_stat_activity
  WHERE pg_stat_activity.datname = db AND pid <> pg_backend_pid();

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;