CREATE OR REPLACE FUNCTION populate_import_temp_tables(p_schema VARCHAR)
    RETURNS void AS $$
BEGIN
    RAISE NOTICE '[%] Populating import_vocabulary_temp table from % schema', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_schema;
    -- Splitting into separate CREATE and INSERT-SELECT statements to populate vocabulary_history_id using a serial type.
    CREATE TEMPORARY TABLE IF NOT EXISTS import_vocabulary_temp (
                                                                    vocabulary_history_id serial,
                                                                    vocabulary_id         varchar(20),
                                                                    vocabulary_name       varchar(255),
                                                                    vocabulary_reference  varchar(255),
                                                                    vocabulary_version    varchar(255),
                                                                    vocabulary_concept_id bigint
    ) ON COMMIT DELETE ROWS;

    EXECUTE format('
        INSERT INTO import_vocabulary_temp(
            vocabulary_id, vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id)
        SELECT
            vocabulary_id, vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id
        FROM %I.vocabulary',
                   p_schema
            );

    RAISE NOTICE '[%] Populating import_concept_temp table from % schema', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_schema;
    CREATE TEMPORARY TABLE IF NOT EXISTS import_concept_temp (
                                                                 concept_id         INTEGER,
                                                                 concept_name       VARCHAR(255),
                                                                 domain_id          VARCHAR(20),
                                                                 vocabulary_id      VARCHAR(20),
                                                                 concept_class_id   VARCHAR(20),
                                                                 standard_concept   VARCHAR(1),
                                                                 concept_code       VARCHAR(50),
                                                                 valid_start_date   DATE,
                                                                 valid_end_date     DATE,
                                                                 invalid_reason     VARCHAR(1),
                                                                 vocabulary_history_id SERIAL
    ) ON COMMIT DELETE ROWS;

    EXECUTE format('
    INSERT INTO import_concept_temp
    SELECT
        c.concept_id,
        c.concept_name,
        c.domain_id,
        c.vocabulary_id,
        c.concept_class_id,
        c.standard_concept,
        c.concept_code,
        c.valid_start_date,
        c.valid_end_date,
        c.invalid_reason,
        v.vocabulary_history_id
    FROM import_vocabulary_temp v
    JOIN %I.concept c ON v.vocabulary_id = c.vocabulary_id', p_schema);
    -- Add the index creation statement
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_concept_id ON import_concept_temp (concept_id)';

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION remove_version_from_history(p_version integer, p_schema text)
    RETURNS void AS
$$
DECLARE
    table_name text;
    partition_table_name text;
    vocabulary_tables CONSTANT text[] := ARRAY[
        'concept_history',
        'concept_ancestor_history',
        'concept_class_history',
        'concept_relationship_history',
        'concept_synonym_history',
        'domain_history',
        'drug_strength_history',
        'relationship_history',
        'vocabulary_history'
        ];
BEGIN
    FOREACH table_name IN ARRAY vocabulary_tables
        LOOP
            partition_table_name := format('%s_%s', table_name, p_version);
            -- Use IF ELSE  to avoid the message "table does not exist, skipping" during DROP IF EXISTS TABLE
            IF EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = p_schema AND t.table_name = partition_table_name) THEN
                EXECUTE format('DROP TABLE IF EXISTS %s.%s CASCADE;', p_schema, partition_table_name);
                RAISE NOTICE '[%] Table %s.%s has been dropped.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_schema, partition_table_name;
            ELSE
                RAISE NOTICE '[%] Table %s.%s does not exist, skipping.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_schema, partition_table_name;
            END IF;
        END LOOP;
    EXECUTE format('DELETE FROM %I.vocabulary_release_version WHERE id = %s', p_schema, p_version);

    RAISE NOTICE '[%] Partitions for version % in schema % have been removed.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_version, p_schema;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_partitions_for_version(p_version integer, p_schema text)
    RETURNS void AS
$$
DECLARE
    table_name text;
    partition_table_name text;
    vocabulary_tables CONSTANT text[] := ARRAY[
        'concept_history',
        'concept_ancestor_history',
        'concept_class_history',
        'concept_relationship_history',
        'concept_synonym_history',
        'domain_history',
        'drug_strength_history',
        'relationship_history',
        'vocabulary_history'
        ];
BEGIN
    FOREACH table_name IN ARRAY vocabulary_tables
        LOOP
            partition_table_name := format('%s_%s', table_name, p_version);
            EXECUTE format('CREATE TABLE %s.%s PARTITION OF %s.%s FOR VALUES IN (%s);', p_schema, partition_table_name, p_schema, table_name, p_version);
            RAISE NOTICE '[%] Partition %s.%s has been created.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_schema, partition_table_name;
        END LOOP;
    RAISE NOTICE '[%] Partitions for version % in schema % have been created.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_version, p_schema;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_vocabulary_version(
    p_schema VARCHAR,
    OUT v_version INTEGER,
    OUT p_version_label VARCHAR(50)
) RETURNS RECORD AS $$
DECLARE
    version_text VARCHAR(50);
    version_date DATE;
BEGIN
    EXECUTE 'SELECT vocabulary_version FROM ' || p_schema || '.vocabulary WHERE vocabulary_id = ''None'' ' INTO version_text;
    version_date := TO_DATE(SUBSTRING(version_text FROM '\d{2}-[A-Za-z]{3}-\d{2}'), 'DD-Mon-YY');

    v_version := EXTRACT(YEAR FROM version_date) * 10000 + EXTRACT(MONTH FROM version_date) * 100 + EXTRACT(DAY FROM version_date);
    p_version_label := version_text;
    RETURN;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_version_to_history(p_version integer, p_version_label text, p_target_schema text, p_source_schema text)
    RETURNS void AS
$$
BEGIN
    RAISE NOTICE '[%] Add new version to the vocabulary_release_version. Version: %, Label: %', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_version, p_version_label;
    EXECUTE format(
            'INSERT INTO %I.vocabulary_release_version (id, vocabulary_name, athena_name, import_datetime) VALUES (%s, %L, %L, clock_timestamp())',
            p_target_schema,
            p_version, 'v'||p_version, p_version_label
            );

    RAISE NOTICE '[%] Concepts...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.concept_history_%s
                SELECT
                    c.concept_id,
                    c.concept_name,
                    c.domain_id,
                    c.vocabulary_id,
                    c.vocabulary_history_id,
                    c.concept_class_id,
                    c.standard_concept,
                    c.concept_code,
                    c.valid_start_date,
                    c.valid_end_date,
                    c.invalid_reason,
                    %s AS version
                FROM import_concept_temp c',
                   p_target_schema, p_version,
                   p_version);

    RAISE NOTICE '[%] Concept Ancestors...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.concept_ancestor_history_%s
                    SELECT ca.*, a.VOCABULARY_HISTORY_ID AS ANCESTOR_VOCABULARY_HISTORY_ID, d.VOCABULARY_HISTORY_ID AS DESCENDANT_HISTORY_VOCABULARY_ID, %s AS version
                    FROM %I.concept_ancestor AS ca
                             JOIN import_concept_temp AS a ON ca.ANCESTOR_CONCEPT_ID = a.CONCEPT_ID
                             JOIN import_concept_temp AS d ON ca.DESCENDANT_CONCEPT_ID = d.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version,
                   p_source_schema);

    RAISE NOTICE '[%] Concept Relationships...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
-- Inserting only half of the concept relationships
    EXECUTE format('INSERT INTO %I.concept_relationship_history_%s
                    SELECT
                        cr.concept_id_1,
                        cr.concept_id_2,
                        rl.relationship_id,
                        rl.reverse_relationship_id,
                        cr.valid_start_date,
                        cr2.valid_start_date AS reverse_valid_start_date,
                        cr.valid_end_date,
                        cr.invalid_reason,
                        c1.VOCABULARY_HISTORY_ID AS VOCABULARY_HISTORY_ID_1,
                        c2.VOCABULARY_HISTORY_ID AS VOCABULARY_HISTORY_ID_2,
                        %s AS VERSION
                    FROM %I.concept_relationship AS cr
                    JOIN %I.relationship AS rl ON cr.relationship_id = rl.relationship_id
                    JOIN import_concept_temp AS c1 ON cr.CONCEPT_ID_1 = c1.CONCEPT_ID
                    JOIN import_concept_temp AS c2 ON cr.CONCEPT_ID_2 = c2.CONCEPT_ID
                    JOIN %I.concept_relationship AS cr2 on cr.CONCEPT_ID_1 = cr2.CONCEPT_ID_2 AND cr.CONCEPT_ID_2 = cr2.CONCEPT_ID_1 AND cr2.relationship_id = reverse_relationship_id
                    WHERE cr.relationship_id > rl.reverse_relationship_id;',
                   p_target_schema, p_version,                             --insert params
                   p_version,                                              --select params
                   p_source_schema, p_source_schema, p_source_schema);     --from/where params

    RAISE NOTICE '[%] Concept Classes...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.concept_class_history_%s
                    SELECT cc.*, %s AS version
                    FROM %I.concept_class cc;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '[%] Concept Synonyms... ', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.concept_synonym_history_%s
                    SELECT cs.*, c.vocabulary_history_id, %s AS version
                    FROM %I.concept_synonym AS cs
                    JOIN import_concept_temp AS c ON cs.CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '[%] Drug Strength... ', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.drug_strength_history_%s
                    SELECT ds.*, c.vocabulary_history_id, %s AS version
                    FROM %I.drug_strength AS ds
                    JOIN import_concept_temp AS c ON ds.DRUG_CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '[%] Domains...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.domain_history_%s
                    SELECT d.*, %s AS version
                    FROM %I.domain d;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '[%] Relationship History...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.relationship_history_%s
                    SELECT r.*, %s AS version
                    FROM %I.relationship r;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '[%] Vocabulary History...', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    EXECUTE format('INSERT INTO %I.vocabulary_history_%s
                    SELECT v.*, %s AS version
                    FROM import_vocabulary_temp v;',
                   p_target_schema, p_version,
                   p_version);

END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION import_new_version(
    p_target_schema VARCHAR,
    p_source_schema VARCHAR
) RETURNS VOID AS
$$
DECLARE
    v_version INTEGER;
    v_version_label VARCHAR(50);

BEGIN

    RAISE NOTICE 'Get new version information...';
--    For some reason this simple SELECT INTO does not work here, as workaround split it to the two queries. SELECT get_vocabulary_version(p_source_schema) INTO v_version, v_version_label
    SELECT (get_vocabulary_version(p_source_schema)).v_version INTO v_version;
    SELECT (get_vocabulary_version(p_source_schema)).p_version_label INTO v_version_label;

    RAISE NOTICE 'Populating import temp tables...';
    PERFORM populate_import_temp_tables(p_source_schema);

    RAISE NOTICE 'Remove version from history...';
    PERFORM remove_version_from_history(v_version, p_target_schema);

    RAISE NOTICE 'Create version in history ...';
    PERFORM create_partitions_for_version(v_version, p_target_schema);

    RAISE NOTICE 'Populate new version to history...';
    PERFORM add_version_to_history(v_version, v_version_label, p_target_schema, p_source_schema);

    RAISE NOTICE 'Refresh delta caches...';
    PERFORM refresh_delta_caches_if_necessary(v_version);

    RAISE NOTICE 'Import process completed successfully.';
END;
$$
LANGUAGE plpgsql;