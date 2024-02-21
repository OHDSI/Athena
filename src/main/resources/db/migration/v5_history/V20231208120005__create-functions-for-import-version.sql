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
                RAISE NOTICE '- Table %s.%s has been dropped.', p_schema, partition_table_name;
            ELSE
                RAISE NOTICE '- Table %s.%s does not exist, skipping.', p_schema, partition_table_name;
            END IF;
        END LOOP;
    EXECUTE format('DELETE FROM %I.vocabulary_release_version WHERE id = %s', p_schema, p_version);

    RAISE NOTICE '- Partitions for version % in schema % have been removed.', p_version, p_schema;
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
            RAISE NOTICE '- Partition %s.%s has been created.', p_schema, partition_table_name;
        END LOOP;
    RAISE NOTICE '- Partitions for version % in schema % have been created.', p_version, p_schema;
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
    RAISE NOTICE '- Add new version to the vocabulary_release_version. Version: %, Label: %', p_version, p_version_label;
    EXECUTE format(
            'INSERT INTO %I.vocabulary_release_version (id, vocabulary_name, athena_name, import_datetime) VALUES (%s, %L, %L, CURRENT_TIMESTAMP)',
            p_target_schema,
            p_version, 'v'||p_version, p_version_label
            );

    RAISE NOTICE '- Concepts...';
    EXECUTE format('INSERT INTO %I.concept_history_%s
                    SELECT c.*, %s AS version FROM %I.concept c;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);


    RAISE NOTICE '- Concept Ancestors...';
    EXECUTE format('INSERT INTO %I.concept_ancestor_history_%s
                    SELECT ca.*, a.VOCABULARY_ID AS ANCESTOR_VOCABULARY_ID, d.VOCABULARY_ID AS DESCENDANT_VOCABULARY_ID, %s AS version
                    FROM %I.concept_ancestor AS ca
                             JOIN %I.CONCEPT AS a ON ca.ANCESTOR_CONCEPT_ID = a.CONCEPT_ID
                             JOIN %I.CONCEPT AS d ON ca.DESCENDANT_CONCEPT_ID = d.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema, p_source_schema);


    RAISE NOTICE '- Concept Relationships...';
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
                        c1.VOCABULARY_ID AS VOCABULARY_ID_1,
                        c2.VOCABULARY_ID AS VOCABULARY_ID_2,
                        %s AS VERSION
                    FROM %I.concept_relationship AS cr
                    JOIN %I.relationship AS rl ON cr.relationship_id = rl.relationship_id
                    JOIN %I.concept AS c1 ON cr.CONCEPT_ID_1 = c1.CONCEPT_ID
                    JOIN %I.concept AS c2 ON cr.CONCEPT_ID_2 = c2.CONCEPT_ID
                    JOIN %I.concept_relationship AS cr2 on cr.CONCEPT_ID_1 = cr2.CONCEPT_ID_2 AND cr.CONCEPT_ID_2 = cr2.CONCEPT_ID_1 AND cr2.relationship_id = reverse_relationship_id
                    WHERE cr.relationship_id > rl.reverse_relationship_id;',
                   p_target_schema, p_version,                                                                        --insert params
                   p_version, p_source_schema, p_source_schema, p_source_schema, p_source_schema, p_source_schema);   --select params


    RAISE NOTICE '- Concept Classes...';
    EXECUTE format('INSERT INTO %I.concept_class_history_%s
                    SELECT cc.*, %s AS version
                    FROM %I.concept_class cc;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);


    RAISE NOTICE '- Concept Synonyms...';
    EXECUTE format('INSERT INTO %I.concept_synonym_history_%s
                    SELECT cs.*, c.vocabulary_id, %s AS version
                    FROM %I.concept_synonym AS cs
                    JOIN %I.concept AS c ON cs.CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema);



    RAISE NOTICE '- Drug Strength...';
    EXECUTE format('INSERT INTO %I.drug_strength_history_%s
                    SELECT ds.*, c.vocabulary_id, %s AS version
                    FROM %I.drug_strength AS ds
                    JOIN %I.concept AS c ON ds.DRUG_CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema);

    RAISE NOTICE '- Domains...';
    EXECUTE format('INSERT INTO %I.domain_history_%s
                    SELECT d.*, %s AS version
                    FROM %I.domain d;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);


    RAISE NOTICE '- Relationship History...';
    EXECUTE format('INSERT INTO %I.relationship_history_%s
                    SELECT r.*, %s AS version
                    FROM %I.relationship r;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

    RAISE NOTICE '- Vocabulary History...';
    EXECUTE format('INSERT INTO %I.vocabulary_history_%s
                    SELECT v.*, %s AS version
                    FROM %I.vocabulary v;',
                   p_target_schema, p_version,
                   p_version, p_source_schema);

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