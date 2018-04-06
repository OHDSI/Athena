
CREATE OR REPLACE FUNCTION cancel_queries_except_current(db VARCHAR)
  RETURNS VOID AS
$body$
BEGIN

  PERFORM pg_cancel_backend(pg_stat_activity.pid)
  FROM pg_stat_activity
  WHERE pg_stat_activity.datname = db AND pid <> pg_backend_pid();

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;