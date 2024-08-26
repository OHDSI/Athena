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


CREATE OR REPLACE FUNCTION compare_schemas(schema1 TEXT, schema2 TEXT)
    RETURNS TABLE (name TEXT, amount1 INTEGER, amount2 INTEGER, missing1 INTEGER, missing2 INTEGER)
AS $$
DECLARE
    table_record RECORD;
    sql_stmt TEXT;
BEGIN
    FOR table_record IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = schema1
        LOOP
            name := table_record.table_name;

            sql_stmt := format('SELECT COUNT(*) FROM %I.%I', schema1, table_record.table_name);
            EXECUTE sql_stmt INTO amount1;

            sql_stmt := format('SELECT COUNT(*) FROM %I.%I', schema2, table_record.table_name);
            EXECUTE sql_stmt INTO amount2;

            sql_stmt := format('SELECT COUNT(*) FROM (SELECT * FROM %I.%I EXCEPT SELECT * FROM %I.%I) AS t',
                               schema2, table_record.table_name, schema1, table_record.table_name);
            EXECUTE sql_stmt INTO missing1;

            sql_stmt := format('SELECT COUNT(*) FROM (SELECT * FROM %I.%I EXCEPT SELECT * FROM %I.%I) AS t',
                               schema1, table_record.table_name, schema2, table_record.table_name);
            EXECUTE sql_stmt INTO missing2;

            RETURN NEXT;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

