CREATE OR REPLACE FUNCTION copy_vocabulary_schema(source_schema TEXT, target_schema TEXT) RETURNS VOID AS $$
DECLARE
    table_name TEXT;
BEGIN
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', target_schema);

    FOR table_name IN
        SELECT t.table_name
        FROM information_schema.tables t
        WHERE t.table_schema = source_schema
        LOOP
            EXECUTE format('CREATE TABLE %I.%I AS SELECT * FROM %I.%I', target_schema, table_name, source_schema, table_name);
        END LOOP;
END;
$$ LANGUAGE plpgsql;
