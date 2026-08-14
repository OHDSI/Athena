-- Delta functions for the three CDM 5.5 tables, built on get_drug_strength_delta
-- (V20231208120003): a full outer join of the two versions on the table's key, keeping
-- only rows whose remaining columns differ, and labelling each with I/U/D plus, for the
-- CSV view, the names of the columns that changed.
--
-- No materialized cache is created. Only concept, concept_relationship and
-- concept_ancestor are cached; everything else is small enough to compute per request,
-- and these three are smaller still.

CREATE OR REPLACE FUNCTION get_concept_metadata_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type    text,
                      attribute_modified text,
                      concept_id         bigint,
                      concept_category   varchar(20),
                      reuse_status       varchar(20)
                  )
AS $$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    RETURN QUERY
        SELECT
            CASE
                WHEN cm2.concept_id IS NULL THEN 'I'
                WHEN cm1.concept_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND cm1.concept_id IS NOT NULL AND cm2.concept_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN cm1.concept_category IS DISTINCT FROM cm2.concept_category THEN 'concept_category' END,
                              CASE WHEN cm1.reuse_status IS DISTINCT FROM cm2.reuse_status THEN 'reuse_status' END
                        )
                END AS attribute_modified,
            COALESCE(cm1.concept_id, cm2.concept_id) AS concept_id,
            cm1.concept_category,
            cm1.reuse_status
        FROM
            (SELECT * FROM concept_metadata_history a1 WHERE a1.version = pVersion1 AND (pVocabularies IS NULL OR a1.vocabulary_history_id = ANY(pVocabulariesHistoryV1))) cm1
                FULL JOIN
            (SELECT * FROM concept_metadata_history a2 WHERE a2.version = pVersion2 AND (pVocabularies IS NULL OR a2.vocabulary_history_id = ANY(pVocabulariesHistoryV2))) cm2
            USING (concept_id)
        WHERE
            ROW(cm1.concept_category, cm1.reuse_status) IS DISTINCT FROM
            ROW(cm2.concept_category, cm2.reuse_status);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_concept_relationship_metadata_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type           text,
                      attribute_modified        text,
                      concept_id_1              bigint,
                      concept_id_2              bigint,
                      relationship_id           varchar(20),
                      relationship_predicate_id varchar(20),
                      relationship_group        integer,
                      mapping_source            varchar(50),
                      confidence                double precision,
                      mapping_tool              varchar(50),
                      mapper                    varchar(50),
                      reviewer                  varchar(50)
                  )
AS $$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    RETURN QUERY
        SELECT
            CASE
                WHEN crm2.concept_id_1 IS NULL THEN 'I'
                WHEN crm1.concept_id_1 IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND crm1.concept_id_1 IS NOT NULL AND crm2.concept_id_1 IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN crm1.relationship_predicate_id IS DISTINCT FROM crm2.relationship_predicate_id THEN 'relationship_predicate_id' END,
                              CASE WHEN crm1.relationship_group IS DISTINCT FROM crm2.relationship_group THEN 'relationship_group' END,
                              CASE WHEN crm1.mapping_source IS DISTINCT FROM crm2.mapping_source THEN 'mapping_source' END,
                              CASE WHEN crm1.confidence IS DISTINCT FROM crm2.confidence THEN 'confidence' END,
                              CASE WHEN crm1.mapping_tool IS DISTINCT FROM crm2.mapping_tool THEN 'mapping_tool' END,
                              CASE WHEN crm1.mapper IS DISTINCT FROM crm2.mapper THEN 'mapper' END,
                              CASE WHEN crm1.reviewer IS DISTINCT FROM crm2.reviewer THEN 'reviewer' END
                        )
                END AS attribute_modified,
            COALESCE(crm1.concept_id_1, crm2.concept_id_1) AS concept_id_1,
            COALESCE(crm1.concept_id_2, crm2.concept_id_2) AS concept_id_2,
            COALESCE(crm1.relationship_id, crm2.relationship_id) AS relationship_id,
            crm1.relationship_predicate_id,
            crm1.relationship_group,
            crm1.mapping_source,
            crm1.confidence,
            crm1.mapping_tool,
            crm1.mapper,
            crm1.reviewer
        FROM
            (SELECT * FROM concept_relationship_metadata_history a1 WHERE a1.version = pVersion1 AND (pVocabularies IS NULL OR (a1.vocabulary_history_id_1 = ANY(pVocabulariesHistoryV1) AND a1.vocabulary_history_id_2 = ANY(pVocabulariesHistoryV1)))) crm1
                FULL JOIN
            (SELECT * FROM concept_relationship_metadata_history a2 WHERE a2.version = pVersion2 AND (pVocabularies IS NULL OR (a2.vocabulary_history_id_1 = ANY(pVocabulariesHistoryV2) AND a2.vocabulary_history_id_2 = ANY(pVocabulariesHistoryV2)))) crm2
            USING (concept_id_1, concept_id_2, relationship_id)
        WHERE
            ROW(crm1.relationship_predicate_id, crm1.relationship_group, crm1.mapping_source, crm1.confidence, crm1.mapping_tool, crm1.mapper, crm1.reviewer) IS DISTINCT FROM
            ROW(crm2.relationship_predicate_id, crm2.relationship_group, crm2.mapping_source, crm2.confidence, crm2.mapping_tool, crm2.mapper, crm2.reviewer);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_pack_content_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type    text,
                      attribute_modified text,
                      pack_concept_id    bigint,
                      drug_concept_id    bigint,
                      amount             smallint,
                      box_size           smallint
                  )
AS $$
DECLARE
    pVocabulariesHistoryV1 integer[];
    pVocabulariesHistoryV2 integer[];
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    pVocabulariesHistoryV1 := get_vocabulary_history_ids(pVocabularies, pVersion1);
    pVocabulariesHistoryV2 := get_vocabulary_history_ids(pVocabularies, pVersion2);
    RETURN QUERY
        SELECT
            CASE
                WHEN pc2.pack_concept_id IS NULL THEN 'I'
                WHEN pc1.pack_concept_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND pc1.pack_concept_id IS NOT NULL AND pc2.pack_concept_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN pc1.amount IS DISTINCT FROM pc2.amount THEN 'amount' END,
                              CASE WHEN pc1.box_size IS DISTINCT FROM pc2.box_size THEN 'box_size' END
                        )
                END AS attribute_modified,
            COALESCE(pc1.pack_concept_id, pc2.pack_concept_id) AS pack_concept_id,
            COALESCE(pc1.drug_concept_id, pc2.drug_concept_id) AS drug_concept_id,
            pc1.amount,
            pc1.box_size
        FROM
            (SELECT * FROM pack_content_history a1 WHERE a1.version = pVersion1 AND (pVocabularies IS NULL OR a1.vocabulary_history_id = ANY(pVocabulariesHistoryV1))) pc1
                FULL JOIN
            (SELECT * FROM pack_content_history a2 WHERE a2.version = pVersion2 AND (pVocabularies IS NULL OR a2.vocabulary_history_id = ANY(pVocabulariesHistoryV2))) pc2
            USING (pack_concept_id, drug_concept_id)
        WHERE
            ROW(pc1.amount, pc1.box_size) IS DISTINCT FROM
            ROW(pc2.amount, pc2.box_size);
END;
$$ LANGUAGE plpgsql;
