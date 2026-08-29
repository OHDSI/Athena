-- History tables for the three CDM 5.5 tables, so that a pinned version and a delta can
-- be served for them the same way they are for drug_strength.
--
-- Partitioned by version like every other history table. Each row carries the
-- vocabulary_history_id of the concept it hangs off, which is what
-- get_vocabulary_history_ids() filters on when a bundle is generated for a selection of
-- vocabularies. concept_relationship_metadata carries two, one per side, mirroring
-- concept_relationship_history.

CREATE TABLE concept_metadata_history
(
    concept_id            bigint,
    concept_category      varchar(20),
    reuse_status          varchar(20),
    vocabulary_history_id integer,
    version               integer
) PARTITION BY LIST (version);

CREATE TABLE concept_relationship_metadata_history
(
    concept_id_1              bigint,
    concept_id_2              bigint,
    relationship_id           varchar(20),
    relationship_predicate_id varchar(20),
    relationship_group        integer,
    mapping_source            varchar(50),
    confidence                double precision,
    mapping_tool              varchar(50),
    mapper                    varchar(50),
    reviewer                  varchar(50),
    vocabulary_history_id_1   integer,
    vocabulary_history_id_2   integer,
    version                   integer
) PARTITION BY LIST (version);

CREATE TABLE pack_content_history
(
    pack_concept_id       bigint,
    drug_concept_id       bigint,
    amount                smallint,
    box_size              smallint,
    vocabulary_history_id integer,
    version               integer
) PARTITION BY LIST (version);


-- The three import functions below are reproduced from V20231208120005 with the new
-- tables added; plpgsql offers no way to amend a function in place.

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
        'vocabulary_history',
        'concept_metadata_history',
        'concept_relationship_metadata_history',
        'pack_content_history'
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
        'vocabulary_history',
        'concept_metadata_history',
        'concept_relationship_metadata_history',
        'pack_content_history'
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

    -- The three below are guarded: a source schema dumped from a release older than
    -- CDM 5.5 does not have these tables, and importing such a version must still work.
    -- The partition is created either way, so the version simply carries no rows for
    -- them and the bundle gets an empty file.

    IF to_regclass(format('%I.%I', p_source_schema, 'concept_metadata')) IS NOT NULL THEN
        RAISE NOTICE '[%] Concept Metadata... ', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
        EXECUTE format('INSERT INTO %I.concept_metadata_history_%s
                        SELECT cm.*, c.vocabulary_history_id, %s AS version
                        FROM %I.concept_metadata AS cm
                        JOIN import_concept_temp AS c ON cm.CONCEPT_ID = c.CONCEPT_ID;',
                       p_target_schema, p_version,
                       p_version, p_source_schema);
    ELSE
        RAISE NOTICE '[%] No concept_metadata in %, skipping.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_source_schema;
    END IF;

    IF to_regclass(format('%I.%I', p_source_schema, 'concept_relationship_metadata')) IS NOT NULL THEN
        RAISE NOTICE '[%] Concept Relationship Metadata... ', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
        EXECUTE format('INSERT INTO %I.concept_relationship_metadata_history_%s
                        SELECT crm.*,
                               c1.VOCABULARY_HISTORY_ID AS VOCABULARY_HISTORY_ID_1,
                               c2.VOCABULARY_HISTORY_ID AS VOCABULARY_HISTORY_ID_2,
                               %s AS version
                        FROM %I.concept_relationship_metadata AS crm
                        JOIN import_concept_temp AS c1 ON crm.CONCEPT_ID_1 = c1.CONCEPT_ID
                        JOIN import_concept_temp AS c2 ON crm.CONCEPT_ID_2 = c2.CONCEPT_ID;',
                       p_target_schema, p_version,
                       p_version, p_source_schema);
    ELSE
        RAISE NOTICE '[%] No concept_relationship_metadata in %, skipping.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_source_schema;
    END IF;

    IF to_regclass(format('%I.%I', p_source_schema, 'pack_content')) IS NOT NULL THEN
        RAISE NOTICE '[%] Pack Content... ', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
        EXECUTE format('INSERT INTO %I.pack_content_history_%s
                        SELECT pc.*, c.vocabulary_history_id, %s AS version
                        FROM %I.pack_content AS pc
                        JOIN import_concept_temp AS c ON pc.PACK_CONCEPT_ID = c.CONCEPT_ID;',
                       p_target_schema, p_version,
                       p_version, p_source_schema);
    ELSE
        RAISE NOTICE '[%] No pack_content in %, skipping.', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS'), p_source_schema;
    END IF;

END;
$$
LANGUAGE plpgsql;
