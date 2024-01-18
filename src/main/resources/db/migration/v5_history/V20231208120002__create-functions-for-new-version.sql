CREATE OR REPLACE FUNCTION removeVersionFromHistory(p_version integer, p_schema text)
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

    table_name := format('%I.concept_relationship_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.domain_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.drug_strength_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.relationship_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    table_name := format('%I.vocabulary_history_%s', p_schema, p_version);
    EXECUTE format('DROP TABLE IF EXISTS %s CASCADE;', table_name);

    DELETE FROM vocabulary_release_version WHERE id = p_version;

    RAISE NOTICE 'Partitions for version % in schema % have been removed.', p_version, p_schema;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION addVersionToHistory(p_version integer, p_target_schema text, p_source_schema text)
    RETURNS void AS
$$
BEGIN
    RAISE NOTICE 'Step 0: Add new version to the vocabulary_release_version...';
    INSERT INTO vocabulary_release_version (id, vocabulary_name, athena_name)
    VALUES (
               p_version,
               TO_CHAR(to_date(CAST(p_version as text), 'YYYYMMDD'), '"v"YYYYMMDD'),
               TO_CHAR(to_date(CAST(p_version as text), 'YYYYMMDD'), '"v5.0 "DD-MON-YY')
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
    EXECUTE format('
        CREATE TABLE %I.concept_relationship_history_%s PARTITION OF %I.concept_relationship_history FOR VALUES IN (%s);
        INSERT INTO %I.concept_relationship_history_%s
        SELECT cr.*, c1.VOCABULARY_ID AS VOCABULARY_ID_1, c2.VOCABULARY_ID AS VOCABULARY_ID_2, %s AS VERSION
        FROM %I.concept_relationship AS cr
                 JOIN %I.CONCEPT AS c1 ON cr.CONCEPT_ID_1 = c1.CONCEPT_ID
                 JOIN %I.CONCEPT AS c2 ON cr.CONCEPT_ID_2 = c2.CONCEPT_ID;',
                   p_target_schema, p_version, p_target_schema, p_version,
                   p_target_schema, p_version,
                   p_version, p_source_schema, p_source_schema, p_source_schema
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
