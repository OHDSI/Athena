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
            WHERE (pVocabularies IS NULL OR (crdc.vocabulary_history_id_1_v1 = ANY(pVocabulariesHistoryV1) AND crdc.vocabulary_history_id_2_v1 = ANY(pVocabulariesHistoryV1)))
              AND (pVocabularies IS NULL OR (crdc.vocabulary_history_id_1_v2 = ANY(pVocabulariesHistoryV2) AND crdc.vocabulary_history_id_2_v2 = ANY(pVocabulariesHistoryV2)));

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
            WHERE (pVocabularies IS NULL OR (crdc2.vocabulary_history_id_1_v1 = ANY(pVocabulariesHistoryV1) AND crdc2.vocabulary_history_id_2_v1 = ANY(pVocabulariesHistoryV1)))
              AND (pVocabularies IS NULL OR (crdc2.vocabulary_history_id_1_v2 = ANY(pVocabulariesHistoryV2) AND crdc2.vocabulary_history_id_2_v2 = ANY(pVocabulariesHistoryV2)));
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