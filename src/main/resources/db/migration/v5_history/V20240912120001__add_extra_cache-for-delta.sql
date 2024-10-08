
CREATE OR REPLACE FUNCTION are_second_latest_and_third_latest(int1 INTEGER, int2 INTEGER)
    RETURNS BOOLEAN AS
$$
BEGIN
    RETURN int1 = get_second_latest_version() AND int2 = get_third_latest_version();
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION is_cached(p_version1 integer, p_version2 integer)
    RETURNS BOOLEAN AS
$$
    -- This function checks if the cache contains data for the VERSION,
-- preventing the use of outdated cache and avoiding the retrieval of incorrect data.
DECLARE
    max_cached_datetime TIMESTAMP;
BEGIN
    SELECT MAX(cached_datetime) INTO max_cached_datetime
    FROM vocabulary_release_version;

    return p_version1 IN (get_latest_version(), get_second_latest_version())
        AND p_version2 IN (get_second_latest_version(), get_third_latest_version())
        AND  EXISTS (SELECT 1
                     FROM vocabulary_release_version
                     WHERE id = p_version1
                       AND cached_datetime = max_cached_datetime
                       AND cached_datetime >= import_datetime
        );
END;
$$ LANGUAGE plpgsql;

CREATE MATERIALIZED VIEW concept_ancestor_delta_cache_3 AS
SELECT * FROM get_concept_ancestor_delta(get_second_latest_version(), get_third_latest_version(), NULL, true)
                  WITH NO DATA;

CREATE MATERIALIZED VIEW concept_delta_cache_3 AS
SELECT * FROM get_concept_delta(get_second_latest_version(), get_third_latest_version(), NULL, true)
                  WITH NO DATA;

CREATE MATERIALIZED VIEW concept_relationship_delta_cache_3 AS
SELECT * FROM get_concept_relationship_delta(get_second_latest_version(), get_third_latest_version(), NULL, true)
                  WITH NO DATA;


CREATE OR REPLACE FUNCTION refresh_delta_caches()
    RETURNS VOID AS $$
DECLARE
    p_cached_datetime timestamp;
    latest_version_id integer;
BEGIN
    RAISE NOTICE '[%] Refreshing concept_ancestor_delta_cache', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_ancestor_delta_cache;

    RAISE NOTICE '[%] Refreshing concept_delta_cache', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_delta_cache;

    RAISE NOTICE '[%] Refreshing concept_relationship_delta_cache', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_relationship_delta_cache;

    RAISE NOTICE '[%] Refreshing concept_ancestor_delta_cache_2', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_ancestor_delta_cache_2;

    RAISE NOTICE '[%] Refreshing concept_delta_cache_2', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_delta_cache_2;

    RAISE NOTICE '[%] Refreshing concept_relationship_delta_cache_2', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_relationship_delta_cache_2;

    RAISE NOTICE '[%] Refreshing concept_ancestor_delta_cache_3', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_ancestor_delta_cache_3;

    RAISE NOTICE '[%] Refreshing concept_delta_cache_3', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_delta_cache_3;

    RAISE NOTICE '[%] Refreshing concept_relationship_delta_cache_3', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
    REFRESH MATERIALIZED VIEW concept_relationship_delta_cache_3;

    p_cached_datetime := clock_timestamp();
    UPDATE vocabulary_release_version SET cached_datetime = p_cached_datetime
    WHERE id in (get_latest_version(), get_second_latest_version(), get_third_latest_version());

    RAISE NOTICE '[%] Cached_datetime updated successfully', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');

    RAISE NOTICE '[%] Caches refreshed successfully', TO_CHAR(clock_timestamp(), 'YYYY-MM-DD HH24:MI:SS');
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION get_concept_delta_cached(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE
            (
                row_change_type     text,
                attribute_modified  text,
                concept_id          bigint,
                concept_name        varchar(255),
                domain_id           varchar(20),
                vocabulary_id       varchar(20),
                concept_class_id    varchar(20),
                standard_concept    varchar(1),
                concept_code        varchar(50),
                valid_start_date    date,
                valid_end_date      date,
                invalid_reason      varchar(1)
            )
AS
$$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    IF is_cached(pVersion1, pVersion2) AND are_latest_and_second_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_delta_cache) THEN
        RAISE NOTICE 'Hit the concept_delta_cache...';
        RETURN QUERY
            SELECT
                c.row_change_type,
                CASE WHEN pCsvView THEN c.attribute_modified END AS attribute_modified,
                c.concept_id,
                c.concept_name,
                c.domain_id,
                c.vocabulary_id,
                c.concept_class_id,
                c.standard_concept,
                c.concept_code,
                c.valid_start_date,
                c.valid_end_date,
                c.invalid_reason
            FROM concept_delta_cache c
            WHERE (vocabulary_history_id_v1 IS NULL OR vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1))
              AND (vocabulary_history_id_v2 IS NULL OR vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2));
    ELSIF are_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_delta_cache_2) THEN
        RAISE NOTICE 'Hit the concept_delta_cache_2...';
        RETURN QUERY
            SELECT
                c.row_change_type,
                CASE WHEN pCsvView THEN c.attribute_modified END AS attribute_modified,
                c.concept_id,
                c.concept_name,
                c.domain_id,
                c.vocabulary_id,
                c.concept_class_id,
                c.standard_concept,
                c.concept_code,
                c.valid_start_date,
                c.valid_end_date,
                c.invalid_reason
            FROM concept_delta_cache_2 c
            WHERE (vocabulary_history_id_v1 IS NULL OR vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1))
              AND (vocabulary_history_id_v2 IS NULL OR vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2));
    ELSIF are_second_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_delta_cache_3) THEN
        RAISE NOTICE 'Hit the concept_delta_cache_3...';
        RETURN QUERY
            SELECT
                c.row_change_type,
                CASE WHEN pCsvView THEN c.attribute_modified END AS attribute_modified,
                c.concept_id,
                c.concept_name,
                c.domain_id,
                c.vocabulary_id,
                c.concept_class_id,
                c.standard_concept,
                c.concept_code,
                c.valid_start_date,
                c.valid_end_date,
                c.invalid_reason
            FROM concept_delta_cache_3 c
            WHERE (vocabulary_history_id_v1 IS NULL OR vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1))
              AND (vocabulary_history_id_v2 IS NULL OR vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2));
    ELSE
        RAISE NOTICE 'Missed the concept_delta_caches...';
        RETURN QUERY
            SELECT
                c.row_change_type,
                c.attribute_modified,
                c.concept_id,
                c.concept_name,
                c.domain_id,
                c.vocabulary_id,
                c.concept_class_id,
                c.standard_concept,
                c.concept_code,
                c.valid_start_date,
                c.valid_end_date,
                c.invalid_reason
            FROM get_concept_delta(pVersion1, pVersion2, pVocabularies, pCsvView) c;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_relationship_delta_cached(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type     text,
                      attribute_modified  text,
                      concept_id_1        bigint,
                      concept_id_2        bigint,
                      relationship_id     varchar(20),
                      valid_start_date    date,
                      valid_end_date      date,
                      invalid_reason      varchar(1)
                  )
AS $$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    IF is_cached(pVersion1, pVersion2) AND are_latest_and_second_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_relationship_delta_cache) THEN
        RAISE NOTICE 'Hit the concept_relationship_delta_cache...';
        RETURN QUERY
            SELECT
                crdc.row_change_type,
                CASE WHEN pCsvView THEN crdc.attribute_modified END AS attribute_modified,
                crdc.concept_id_1,
                crdc.concept_id_2,
                crdc.relationship_id,
                crdc.valid_start_date,
                crdc.valid_end_date,
                crdc.invalid_reason
            FROM concept_relationship_delta_cache crdc
            WHERE (crdc.vocabulary_history_id_1_v1 IS NULL OR (crdc.vocabulary_history_id_1_v1 = ANY(pVocabulariesHistoryV1) AND crdc.vocabulary_history_id_2_v1 = ANY(pVocabulariesHistoryV1)))
              AND (crdc.vocabulary_history_id_2_v2 IS NULL OR (crdc.vocabulary_history_id_2_v2 = ANY(pVocabulariesHistoryV2) AND crdc.vocabulary_history_id_2_v2 = ANY(pVocabulariesHistoryV2)));

    ELSIF are_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_relationship_delta_cache_2) THEN
        RAISE NOTICE 'Hit the concept_relationship_delta_cache_2...';
        RETURN QUERY
            SELECT
                crdc2.row_change_type,
                CASE WHEN pCsvView THEN crdc2.attribute_modified END AS attribute_modified,
                crdc2.concept_id_1,
                crdc2.concept_id_2,
                crdc2.relationship_id,
                crdc2.valid_start_date,
                crdc2.valid_end_date,
                crdc2.invalid_reason
            FROM concept_relationship_delta_cache_2 crdc2
            WHERE (crdc2.vocabulary_history_id_1_v1 IS NULL OR (crdc2.vocabulary_history_id_1_v1 = ANY(pVocabulariesHistoryV1) AND crdc2.vocabulary_history_id_2_v1 = ANY(pVocabulariesHistoryV1)))
              AND (crdc2.vocabulary_history_id_1_v2 IS NULL OR (crdc2.vocabulary_history_id_1_v2 = ANY(pVocabulariesHistoryV2) AND crdc2.vocabulary_history_id_2_v2 = ANY(pVocabulariesHistoryV2)));

    ELSIF are_second_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_relationship_delta_cache_3) THEN
        RAISE NOTICE 'Hit the concept_relationship_delta_cache_3...';
        RETURN QUERY
            SELECT
                crdc3.row_change_type,
                CASE WHEN pCsvView THEN crdc3.attribute_modified END AS attribute_modified,
                crdc3.concept_id_1,
                crdc3.concept_id_2,
                crdc3.relationship_id,
                crdc3.valid_start_date,
                crdc3.valid_end_date,
                crdc3.invalid_reason
            FROM concept_relationship_delta_cache_3 crdc3
            WHERE (crdc3.vocabulary_history_id_1_v1 IS NULL OR (crdc3.vocabulary_history_id_1_v1 = ANY (pVocabulariesHistoryV1) AND crdc3.vocabulary_history_id_2_v1 = ANY (pVocabulariesHistoryV1)))
              AND (crdc3.vocabulary_history_id_1_v2 IS NULL OR (crdc3.vocabulary_history_id_1_v2 = ANY (pVocabulariesHistoryV2) AND crdc3.vocabulary_history_id_2_v2 = ANY (pVocabulariesHistoryV2)));
    ELSE
        RAISE NOTICE 'Missed the concept_relationship_delta_caches...';
        RETURN QUERY
            SELECT
                crc.row_change_type,
                crc.attribute_modified,
                crc.concept_id_1,
                crc.concept_id_2,
                crc.relationship_id,
                crc.valid_start_date,
                crc.valid_end_date,
                crc.invalid_reason
            FROM get_concept_relationship_delta(pVersion1, pVersion2, pVocabularies, pCsvView) crc;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_ancestor_delta_cached(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type             text,
                      attribute_modified          text,
                      ancestor_concept_id         bigint,
                      descendant_concept_id       bigint,
                      min_levels_of_separation    bigint,
                      max_levels_of_separation    bigint
                  )
AS $$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    IF is_cached(pVersion1, pVersion2) AND are_latest_and_second_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_ancestor_delta_cache) THEN
        RAISE NOTICE 'Hit the concept_ancestor_delta_cache...';
        RETURN QUERY
            SELECT
                cadc.row_change_type,
                CASE WHEN pCsvView THEN cadc.attribute_modified END AS attribute_modified,
                cadc.ancestor_concept_id,
                cadc.descendant_concept_id,
                cadc.min_levels_of_separation,
                cadc.max_levels_of_separation
            FROM concept_ancestor_delta_cache cadc
            WHERE (cadc.ancestor_vocabulary_history_id_v1 IS NULL OR (cadc.ancestor_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1) AND cadc.descendant_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1)))
              AND (cadc.ancestor_vocabulary_history_id_v2 IS NULL OR (cadc.ancestor_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2) AND cadc.descendant_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2)));

    ELSIF are_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_ancestor_delta_cache_2) THEN
        RAISE NOTICE 'Hit the concept_ancestor_delta_cache_2...';
        RETURN QUERY
            SELECT
                cadc2.row_change_type,
                CASE WHEN pCsvView THEN cadc2.attribute_modified END AS attribute_modified,
                cadc2.ancestor_concept_id,
                cadc2.descendant_concept_id,
                cadc2.min_levels_of_separation,
                cadc2.max_levels_of_separation
            FROM concept_ancestor_delta_cache_2 cadc2
            WHERE (cadc2.ancestor_vocabulary_history_id_v1 IS NULL OR (cadc2.ancestor_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1) AND cadc2.descendant_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1)))
              AND (cadc2.ancestor_vocabulary_history_id_v2 IS NULL OR (cadc2.ancestor_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2) AND cadc2.descendant_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2)));

    ELSIF are_second_latest_and_third_latest(pVersion1, pVersion2) AND EXISTS (SELECT 1 FROM concept_ancestor_delta_cache_3) THEN
        RAISE NOTICE 'Hit the concept_ancestor_delta_cache_2...';
        RETURN QUERY
            SELECT
                cadc3.row_change_type,
                CASE WHEN pCsvView THEN cadc3.attribute_modified END AS attribute_modified,
                cadc3.ancestor_concept_id,
                cadc3.descendant_concept_id,
                cadc3.min_levels_of_separation,
                cadc3.max_levels_of_separation
            FROM concept_ancestor_delta_cache_2 cadc3
            WHERE (cadc3.ancestor_vocabulary_history_id_v1 IS NULL OR (cadc3.ancestor_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1) AND cadc3.descendant_vocabulary_history_id_v1 = ANY(pVocabulariesHistoryV1)))
              AND (cadc3.ancestor_vocabulary_history_id_v2 IS NULL OR (cadc3.ancestor_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2) AND cadc3.descendant_vocabulary_history_id_v2 = ANY(pVocabulariesHistoryV2)));
    ELSE
        RAISE NOTICE 'Missed the concept_ancestor_delta_caches...';
        RETURN QUERY
            SELECT
                cad.row_change_type,
                cad.attribute_modified,
                cad.ancestor_concept_id,
                cad.descendant_concept_id,
                cad.min_levels_of_separation,
                cad.max_levels_of_separation
            FROM get_concept_ancestor_delta(pVersion1, pVersion2, pVocabularies, pCsvView) cad;
    END IF;
END;
$$ LANGUAGE plpgsql;

