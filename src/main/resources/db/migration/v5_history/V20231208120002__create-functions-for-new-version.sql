CREATE OR REPLACE FUNCTION remove_version_from_history(p_version integer, p_schema text)
    RETURNS void AS
$$
DECLARE
    table_name text;
BEGIN

    table_name := format('%I.concept_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.concept_ancestor_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.concept_class_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.concept_relationship_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.concept_synonym_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.domain_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.drug_strength_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.relationship_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.vocabulary_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    EXECUTE format('DELETE FROM %I.vocabulary_release_version WHERE id = %s', p_schema, p_version, p_schema);
    RAISE NOTICE 'Partitions for version % in schema % have been removed.', p_version, p_schema;
END;
$$
LANGUAGE plpgsql;

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
    RAISE NOTICE 'Step 0: Add new version to the vocabulary_release_version. Version: %, Label: %', p_version, p_version_label;
    EXECUTE format(
            'INSERT INTO %I.vocabulary_release_version (id, vocabulary_name, athena_name) VALUES (%s, %L, %L)',
            p_target_schema,
            p_version, 'v'||p_version, p_version_label
    );

    RAISE NOTICE 'Step 1: Concepts...';
    EXECUTE format('
        CREATE TABLE %I.concept_history_%s PARTITION OF %I.concept_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_history_%s
        SELECT c.*, %s AS version
        FROM %I.concept c;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema
        );

    RAISE NOTICE 'Step 2: Concept Ancestors...';
    EXECUTE format('
        CREATE TABLE %I.concept_ancestor_history_%s PARTITION OF %I.concept_ancestor_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_ancestor_history_%s
        SELECT ca.*, a.VOCABULARY_ID AS ANCESTOR_VOCABULARY_ID, d.VOCABULARY_ID AS DESCENDANT_VOCABULARY_ID, %s AS version
        FROM %I.concept_ancestor AS ca
                 JOIN %I.CONCEPT AS a ON ca.ANCESTOR_CONCEPT_ID = a.CONCEPT_ID
                 JOIN %I.CONCEPT AS d ON ca.DESCENDANT_CONCEPT_ID = d.CONCEPT_ID;',
                    p_target_schema, p_version, p_target_schema, p_version,
                    p_target_schema, p_version,
                    p_version, p_source_schema, p_source_schema, p_source_schema
        );

    RAISE NOTICE 'Step 3: Concept Relationships...';
    -- Inserting only half of the concept relationships
    EXECUTE format('
        CREATE TABLE %I.concept_relationship_history_%s PARTITION OF %I.concept_relationship_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_relationship_history_%s
        SELECT
            cr.concept_id_1,
            cr.concept_id_2,
            rl.relationship_id,
            rl.reverse_relationship_id,
            cr.valid_start_date,
            cr.valid_end_date,
            cr.invalid_reason,
            c1.VOCABULARY_ID AS VOCABULARY_ID_1,
            c2.VOCABULARY_ID AS VOCABULARY_ID_2,
            %s AS VERSION
        FROM %I.concept_relationship AS cr
        JOIN %I.relationship AS rl ON cr.relationship_id = rl.relationship_id
        JOIN %I.concept AS c1 ON cr.CONCEPT_ID_1 = c1.CONCEPT_ID
        JOIN %I.concept AS c2 ON cr.CONCEPT_ID_2 = c2.CONCEPT_ID
        WHERE cr.relationship_id > rl.reverse_relationship_id ',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema, p_source_schema, p_source_schema
            );


    RAISE NOTICE 'Step 4: Concept Classes...';
    EXECUTE format('
        CREATE TABLE %I.concept_class_history_%s PARTITION OF %I.concept_class_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_class_history_%s
        SELECT cc.*, %s AS version
        FROM %I.concept_class cc;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema
        );

    RAISE NOTICE 'Step 5: Concept Synonyms...';
    EXECUTE format('
        CREATE TABLE %I.concept_synonym_history_%s PARTITION OF %I.concept_synonym_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_synonym_history_%s
        SELECT cs.*, c.vocabulary_id, %s AS version
        FROM %I.concept_synonym AS cs
                 JOIN %I.concept AS c ON cs.CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema
        );

    RAISE NOTICE 'Step 6: Drug Strength...';
    EXECUTE format('
        CREATE TABLE %I.drug_strength_history_%s PARTITION OF %I.drug_strength_history FOR VALUES IN (%s);
        INSERT INTO %I.drug_strength_history_%s
        SELECT ds.*, c.vocabulary_id, %s AS version
        FROM %I.drug_strength AS ds
                 JOIN %I.concept AS c ON ds.DRUG_CONCEPT_ID = c.CONCEPT_ID;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema
        );

    RAISE NOTICE 'Step 7: Domains...';
    EXECUTE format('
        CREATE TABLE %I.domain_history_%s PARTITION OF %I.domain_history FOR VALUES IN (%s);
        INSERT INTO %I.domain_history_%s
        SELECT d.*, %s AS version
        FROM %I.domain d;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema
        );

    RAISE NOTICE 'Step 8: Relationship History...';
    EXECUTE format('
        CREATE TABLE %I.relationship_history_%s PARTITION OF %I.relationship_history FOR VALUES IN (%s);
        INSERT INTO %I.relationship_history_%s
        SELECT r.*, %s AS version
        FROM %I.relationship r;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema
        );

    RAISE NOTICE 'Step 9: Vocabulary History...';
    EXECUTE format('
        CREATE TABLE %I.vocabulary_history_%s PARTITION OF %I.vocabulary_history FOR VALUES IN (%s);
        INSERT INTO %I.vocabulary_history_%s
        SELECT v.*, %s AS version
        FROM %I.vocabulary v;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema
        );
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

    RAISE NOTICE 'Remove version % from history...', v_version;
    PERFORM remove_version_from_history(v_version, p_target_schema);

    RAISE NOTICE 'Add new version to history...';
    PERFORM add_version_to_history(v_version, v_version_label, p_target_schema, p_source_schema);

    RAISE NOTICE 'Import process completed successfully.';
END;
$$
LANGUAGE plpgsql;