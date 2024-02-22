CREATE OR REPLACE FUNCTION get_vocabulary_history_ids(
    pVocabularies text[],
    pVersion integer
)
    RETURNS integer[]
AS
$$
DECLARE
    result integer[];
BEGIN
    SELECT ARRAY_AGG(vocabulary_history_id)
    INTO result
    FROM vocabulary_history
    WHERE version = pVersion AND (pVocabularies IS NULL OR vocabulary_id = ANY(pVocabularies));

    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE
            (
                row_change_type          text,
                attribute_modified       text,
                concept_id               bigint,
                concept_name             varchar(255),
                domain_id                varchar(20),
                vocabulary_id            varchar(20),
                concept_class_id         varchar(20),
                standard_concept         varchar(1),
                concept_code             varchar(50),
                valid_start_date         date,
                valid_end_date           date,
                invalid_reason           varchar(1),
                vocabulary_history_id_v1 integer,
                vocabulary_history_id_v2 integer
            )
AS
$$
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
                WHEN c2.concept_id IS NULL THEN 'I'
                WHEN c1.concept_id = c2.concept_id THEN 'U'
                WHEN c1.concept_id IS NULL THEN 'D'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND c1.concept_id IS NOT NULL AND c2.concept_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN c1.concept_name IS DISTINCT FROM c2.concept_name THEN 'concept_name' END,
                              CASE WHEN c1.domain_id IS DISTINCT FROM c2.domain_id THEN 'domain_id' END,
                              CASE WHEN c1.vocabulary_id IS DISTINCT FROM c2.vocabulary_id THEN 'vocabulary_id' END,
                              CASE WHEN c1.concept_class_id IS DISTINCT FROM c2.concept_class_id THEN 'concept_class_id' END,
                              CASE WHEN c1.standard_concept IS DISTINCT FROM c2.standard_concept THEN 'standard_concept' END,
                              CASE WHEN c1.concept_code IS DISTINCT FROM c2.concept_code THEN 'concept_code' END,
                              CASE WHEN c1.valid_start_date IS DISTINCT FROM c2.valid_start_date THEN 'valid_start_date' END,
                              CASE WHEN c1.valid_end_date IS DISTINCT FROM c2.valid_end_date THEN 'valid_end_date' END,
                              CASE WHEN c1.invalid_reason IS DISTINCT FROM c2.invalid_reason THEN 'invalid_reason' END
                    )
                END AS attribute_modified,
            COALESCE(c1.concept_id, c2.concept_id) AS concept_id,
            c1.concept_name,
            c1.domain_id,
            c1.vocabulary_id,
            c1.concept_class_id,
            c1.standard_concept,
            c1.concept_code,
            c1.valid_start_date,
            c1.valid_end_date,
            c1.invalid_reason,
            c1.vocabulary_history_id vocabulary_history_id_v1,
            c2.vocabulary_history_id vocabulary_history_id_v2
        FROM
            (SELECT * FROM concept_history a1 WHERE a1.version = pVersion1 AND(pVocabularies IS NULL OR a1.vocabulary_history_id=ANY(pVocabulariesHistoryV1))) c1
                FULL JOIN
            (SELECT * FROM concept_history a2 WHERE a2.version = pVersion2 AND(pVocabularies IS NULL OR  a2.vocabulary_history_id=ANY(pVocabulariesHistoryV2))) c2
            USING (concept_id)
        WHERE
            ROW(c1.concept_name, c1.domain_id, c1.vocabulary_id, c1.concept_class_id, c1.standard_concept, c1.concept_code, c1.valid_start_date, c1.valid_end_date, c1.invalid_reason) IS DISTINCT FROM
            ROW(c2.concept_name, c2.domain_id, c2.vocabulary_id, c2.concept_class_id, c2.standard_concept, c2.concept_code, c2.valid_start_date, c2.valid_end_date, c2.invalid_reason);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_relationship_delta(
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
                      invalid_reason      varchar(1),
                      vocabulary_history_id_1_v1  integer,
                      vocabulary_history_id_2_v1  integer,
                      vocabulary_history_id_1_v2  integer,
                      vocabulary_history_id_2_v2  integer
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
        WITH concept_relationship_changes AS (
            SELECT
                CASE
                    WHEN c2.concept_id_1 IS NULL THEN 'I'
                    WHEN c1.concept_id_1 IS NULL THEN 'D'
                    ELSE 'U'
                    END AS row_change_type,
                CASE
                    WHEN pCsvView AND c1.concept_id_1 IS NOT NULL AND c2.concept_id_1 IS NOT NULL THEN
                        CONCAT_WS(', ',
                                  CASE WHEN c1.valid_start_date IS DISTINCT FROM c2.valid_start_date THEN 'valid_start_date' END,
                                  CASE WHEN c1.valid_end_date IS DISTINCT FROM c2.valid_end_date THEN 'valid_end_date' END,
                                  CASE WHEN c1.invalid_reason IS DISTINCT FROM c2.invalid_reason THEN 'invalid_reason' END
                        )
                    END AS attribute_modified,
                COALESCE(c1.concept_id_1, c2.concept_id_1) AS concept_id_1,
                COALESCE(c1.concept_id_2, c2.concept_id_2) AS concept_id_2,
                COALESCE(c1.relationship_id, c2.relationship_id) AS relationship_id,
                COALESCE(c1.reverse_relationship_id, c2.reverse_relationship_id) AS reverse_relationship_id,
                c1.valid_start_date,
                c1.reverse_valid_start_date,
                c1.valid_end_date,
                c1.invalid_reason,
                c1.vocabulary_history_id_1 vocabulary_history_id_1_v1,
                c1.vocabulary_history_id_2 vocabulary_history_id_2_v1,
                c2.vocabulary_history_id_1 vocabulary_history_id_1_v2,
                c2.vocabulary_history_id_2 vocabulary_history_id_2_v2
            FROM
                (SELECT * FROM concept_relationship_history a1 WHERE a1.version = pVersion1 AND (pVocabularies IS NULL OR (a1.vocabulary_history_id_1 = ANY(pVocabulariesHistoryV1) AND a1.vocabulary_history_id_2 = ANY(pVocabulariesHistoryV1)))) c1
                    FULL JOIN
                (SELECT * FROM concept_relationship_history a2 WHERE a2.version = pVersion2 AND (pVocabularies IS NULL OR (a2.vocabulary_history_id_1 = ANY(pVocabulariesHistoryV2) AND a2.vocabulary_history_id_2 = ANY(pVocabulariesHistoryV2)))) c2
                USING (concept_id_1, concept_id_2, relationship_id)
            WHERE
                ROW(c1.valid_start_date, c1.valid_end_date, c1.invalid_reason) IS DISTINCT FROM
                ROW(c2.valid_start_date, c2.valid_end_date, c2.invalid_reason)
        )
        SELECT
            crc1.row_change_type,
            crc1.attribute_modified,
            crc1.concept_id_1,
            crc1.concept_id_2,
            crc1.relationship_id,
            crc1.valid_start_date,
            crc1.valid_end_date,
            crc1.invalid_reason,
            crc1.vocabulary_history_id_1_v1,
            crc1.vocabulary_history_id_2_v1,
            crc1.vocabulary_history_id_1_v2,
            crc1.vocabulary_history_id_2_v2
        FROM concept_relationship_changes crc1
        UNION ALL
        -- reversed relationships
        SELECT
            crc2.row_change_type,
            crc2.attribute_modified,
            crc2.concept_id_2 AS concept_id_1,
            crc2.concept_id_1 AS concept_id_2,
            crc2.reverse_relationship_id as relationship_id,
            crc2.reverse_valid_start_date AS valid_end_date,
            crc2.valid_end_date,
            crc2.invalid_reason,
            crc2.vocabulary_history_id_1_v1,
            crc2.vocabulary_history_id_2_v1,
            crc2.vocabulary_history_id_1_v2,
            crc2.vocabulary_history_id_2_v2
     FROM concept_relationship_changes crc2;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_ancestor_delta(
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
                      max_levels_of_separation    bigint,
                      ancestor_vocabulary_history_id_v1   integer,
                      descendant_vocabulary_history_id_v1 integer,
                      ancestor_vocabulary_history_id_v2   integer,
                      descendant_vocabulary_history_id_v2 integer
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
                WHEN c2.ancestor_concept_id IS NULL THEN 'I'
                WHEN c1.ancestor_concept_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND c1.ancestor_concept_id IS NOT NULL AND c2.ancestor_concept_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN c1.ancestor_concept_id IS DISTINCT FROM c2.ancestor_concept_id THEN 'ancestor_concept_id' END,
                              CASE WHEN c1.descendant_concept_id IS DISTINCT FROM c2.descendant_concept_id THEN 'descendant_concept_id' END,
                              CASE WHEN c1.min_levels_of_separation IS DISTINCT FROM c2.min_levels_of_separation THEN 'min_levels_of_separation' END,
                              CASE WHEN c1.max_levels_of_separation IS DISTINCT FROM c2.max_levels_of_separation THEN 'max_levels_of_separation' END
                        )
                END AS attribute_modified,
            COALESCE(c1.ancestor_concept_id, c2.ancestor_concept_id) AS ancestor_concept_id,
            COALESCE(c1.descendant_concept_id, c2.descendant_concept_id) AS descendant_concept_id,
            c1.min_levels_of_separation,
            c1.max_levels_of_separation,
            c1.ancestor_vocabulary_history_id    ancestor_vocabulary_id_v1,
            c1.descendant_vocabulary_history_id  descendant_vocabulary_id_v1,
            c2.ancestor_vocabulary_history_id    ancestor_vocabulary_id_v2,
            c2.descendant_vocabulary_history_id  descendant_vocabulary_id_v2
        FROM
            (SELECT * FROM concept_ancestor_history a1 WHERE a1.version = pVersion1 AND (pVocabularies IS NULL OR (a1.ancestor_vocabulary_history_id = ANY(pVocabulariesHistoryV1) AND a1.descendant_vocabulary_history_id = ANY(pVocabulariesHistoryV1)))) c1
                FULL JOIN
            (SELECT * FROM concept_ancestor_history a2 WHERE a2.version = pVersion2 AND (pVocabularies IS NULL OR (a2.ancestor_vocabulary_history_id = ANY(pVocabulariesHistoryV2) AND a2.descendant_vocabulary_history_id = ANY(pVocabulariesHistoryV2)))) c2
            USING (ancestor_concept_id, descendant_concept_id)
        WHERE
            ROW(c1.min_levels_of_separation, c1.max_levels_of_separation) IS DISTINCT FROM
            ROW(c2.min_levels_of_separation, c2.max_levels_of_separation);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_synonym_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type         text,
                      attribute_modified      text,
                      concept_id              bigint,
                      concept_synonym_name    varchar(1000),
                      language_concept_id     bigint
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
                WHEN c2.concept_id IS NULL THEN 'I'
                WHEN c1.concept_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            NULL AS attribute_modified, -- There is not field to modify, the all fields are used in the join
            COALESCE(c1.concept_id, c2.concept_id) AS concept_id,
            COALESCE(c1.concept_synonym_name, c2.concept_synonym_name) AS concept_synonym_name,
            COALESCE(c1.language_concept_id, c2.language_concept_id) AS language_concept_id
        FROM
            (SELECT * FROM concept_synonym_history a1 WHERE a1.version = pVersion1 AND a1.vocabulary_history_id = ANY(pVocabulariesHistoryV1)) c1
                FULL JOIN
            (SELECT * FROM concept_synonym_history a2 WHERE a2.version = pVersion2 AND a2.vocabulary_history_id = ANY(pVocabulariesHistoryV2)) c2
            USING (concept_id, concept_synonym_name, language_concept_id);

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_domain_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type    text,
                      attribute_modified text,
                      domain_id          varchar(20),
                      domain_name        varchar(255),
                      domain_concept_id  bigint
                  )
AS $$
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    RETURN QUERY
        SELECT
            CASE
                WHEN d2.domain_id IS NULL THEN 'I'
                WHEN d1.domain_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND d1.domain_id IS NOT NULL AND d2.domain_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN d1.domain_name IS DISTINCT FROM d2.domain_name THEN 'domain_name' END,
                              CASE WHEN d1.domain_concept_id IS DISTINCT FROM d2.domain_concept_id THEN 'domain_concept_id' END
                        )
                END AS attribute_modified,
            COALESCE(d1.domain_id, d2.domain_id) AS domain_id,
            d1.domain_name,
            d1.domain_concept_id
        FROM
            (SELECT * FROM domain_history a1 WHERE a1.version = pVersion1) d1
                FULL JOIN
            (SELECT * FROM domain_history a2 WHERE a2.version = pVersion2) d2
            USING (domain_id)
        WHERE
            ROW(d1.domain_name, d1.domain_concept_id) IS DISTINCT FROM
            ROW(d2.domain_name, d2.domain_concept_id);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_relationship_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type             text,
                      attribute_modified          text,
                      relationship_id             varchar(20),
                      relationship_name           varchar(255),
                      is_hierarchical             varchar(1),
                      defines_ancestry            varchar(1),
                      reverse_relationship_id     varchar(20),
                      relationship_concept_id     bigint
                  )
AS $$
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    RETURN QUERY
        SELECT
            CASE
                WHEN r2.relationship_id IS NULL THEN 'I'
                WHEN r1.relationship_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND r1.relationship_id IS NOT NULL AND r2.relationship_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN r1.relationship_name IS DISTINCT FROM r2.relationship_name THEN 'relationship_name' END,
                              CASE WHEN r1.is_hierarchical IS DISTINCT FROM r2.is_hierarchical THEN 'is_hierarchical' END,
                              CASE WHEN r1.defines_ancestry IS DISTINCT FROM r2.defines_ancestry THEN 'defines_ancestry' END,
                              CASE WHEN r1.reverse_relationship_id IS DISTINCT FROM r2.reverse_relationship_id THEN 'reverse_relationship_id' END,
                              CASE WHEN r1.relationship_concept_id IS DISTINCT FROM r2.relationship_concept_id THEN 'relationship_concept_id' END
                        )
                END AS attribute_modified,
            COALESCE(r1.relationship_id, r2.relationship_id) AS relationship_id,
            r1.relationship_name,
            r1.is_hierarchical,
            r1.defines_ancestry,
            r1.reverse_relationship_id,
            r1.relationship_concept_id
        FROM
            (SELECT * FROM relationship_history a1 WHERE a1.version = pVersion1) r1
                FULL JOIN
            (SELECT * FROM relationship_history a2 WHERE a2.version = pVersion2) r2
            USING (relationship_id)
        WHERE
            ROW(r1.relationship_name, r1.is_hierarchical, r1.defines_ancestry, r1.reverse_relationship_id, r1.relationship_concept_id) IS DISTINCT FROM
            ROW(r2.relationship_name, r2.is_hierarchical, r2.defines_ancestry, r2.reverse_relationship_id, r2.relationship_concept_id);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_vocabulary_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type             text,
                      attribute_modified          text,
                      vocabulary_id               varchar(20),
                      vocabulary_name             varchar(255),
                      vocabulary_reference        varchar(255),
                      vocabulary_version          varchar(255),
                      vocabulary_concept_id       bigint
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
                WHEN v2.vocabulary_id IS NULL THEN 'I'
                WHEN v1.vocabulary_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND v1.vocabulary_id IS NOT NULL AND v2.vocabulary_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN v1.vocabulary_name IS DISTINCT FROM v2.vocabulary_name THEN 'vocabulary_name' END,
                              CASE WHEN v1.vocabulary_reference IS DISTINCT FROM v2.vocabulary_reference THEN 'vocabulary_reference' END,
                              CASE WHEN v1.vocabulary_version IS DISTINCT FROM v2.vocabulary_version THEN 'vocabulary_version' END,
                              CASE WHEN v1.vocabulary_concept_id IS DISTINCT FROM v2.vocabulary_concept_id THEN 'vocabulary_concept_id' END
                        )
                END AS attribute_modified,
            COALESCE(v1.vocabulary_id, v2.vocabulary_id) AS vocabulary_id,
            v1.vocabulary_name,
            v1.vocabulary_reference,
            v1.vocabulary_version,
            v1.vocabulary_concept_id
        FROM
            (SELECT * FROM vocabulary_history a1 WHERE a1.version = pVersion1 AND a1.vocabulary_history_id = ANY(pVocabulariesHistoryV1)) v1
                FULL JOIN
            (SELECT * FROM vocabulary_history a2 WHERE a2.version = pVersion2 AND a2.vocabulary_history_id = ANY(pVocabulariesHistoryV2) ) v2
            USING (vocabulary_id)
        WHERE
            ROW(v1.vocabulary_name, v1.vocabulary_reference, v1.vocabulary_version, v1.vocabulary_concept_id) IS DISTINCT FROM
            ROW(v2.vocabulary_name, v2.vocabulary_reference, v2.vocabulary_version, v2.vocabulary_concept_id);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_drug_strength_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type                   text,
                      attribute_modified                text,
                      drug_concept_id                   bigint,
                      ingredient_concept_id             bigint,
                      amount_value                      numeric,
                      amount_unit_concept_id            bigint,
                      numerator_value                   numeric,
                      numerator_unit_concept_id         bigint,
                      denominator_value                 numeric,
                      denominator_unit_concept_id       bigint,
                      box_size                          integer,
                      valid_start_date                  date,
                      valid_end_date                    date,
                      invalid_reason                    varchar(1)
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
                WHEN ds2.drug_concept_id IS NULL THEN 'I'
                WHEN ds1.drug_concept_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND ds1.drug_concept_id IS NOT NULL AND ds2.drug_concept_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN ds1.amount_value IS DISTINCT FROM ds2.amount_value THEN 'amount_value' END,
                              CASE WHEN ds1.amount_unit_concept_id IS DISTINCT FROM ds2.amount_unit_concept_id THEN 'amount_unit_concept_id' END,
                              CASE WHEN ds1.numerator_value IS DISTINCT FROM ds2.numerator_value THEN 'numerator_value' END,
                              CASE WHEN ds1.numerator_unit_concept_id IS DISTINCT FROM ds2.numerator_unit_concept_id THEN 'numerator_unit_concept_id' END,
                              CASE WHEN ds1.denominator_value IS DISTINCT FROM ds2.denominator_value THEN 'denominator_value' END,
                              CASE WHEN ds1.denominator_unit_concept_id IS DISTINCT FROM ds2.denominator_unit_concept_id THEN 'denominator_unit_concept_id' END,
                              CASE WHEN ds1.box_size IS DISTINCT FROM ds2.box_size THEN 'box_size' END,
                              CASE WHEN ds1.valid_start_date IS DISTINCT FROM ds2.valid_start_date THEN 'valid_start_date' END,
                              CASE WHEN ds1.valid_end_date IS DISTINCT FROM ds2.valid_end_date THEN 'valid_end_date' END,
                              CASE WHEN ds1.invalid_reason IS DISTINCT FROM ds2.invalid_reason THEN 'invalid_reason' END
                        )
                END AS attribute_modified,
            COALESCE(ds1.drug_concept_id, ds2.drug_concept_id) AS drug_concept_id,
            COALESCE(ds1.ingredient_concept_id, ds2.ingredient_concept_id) AS ingredient_concept_id,
            ds1.amount_value,
            ds1.amount_unit_concept_id,
            ds1.numerator_value,
            ds1.numerator_unit_concept_id,
            ds1.denominator_value,
            ds1.denominator_unit_concept_id,
            ds1.box_size,
            ds1.valid_start_date,
            ds1.valid_end_date,
            ds1.invalid_reason
        FROM
            (SELECT * FROM drug_strength_history a1 WHERE a1.version = pVersion1 AND a1.vocabulary_history_id = ANY(pVocabulariesHistoryV1)) ds1
                FULL JOIN
            (SELECT * FROM drug_strength_history a2 WHERE a2.version = pVersion2 AND a2.vocabulary_history_id = ANY(pVocabulariesHistoryV2)) ds2
            USING (drug_concept_id, ingredient_concept_id)
        WHERE
            ROW(ds1.amount_value, ds1.amount_unit_concept_id, ds1.numerator_value, ds1.numerator_unit_concept_id, ds1.denominator_value, ds1.denominator_unit_concept_id, ds1.box_size, ds1.valid_start_date, ds1.valid_end_date, ds1.invalid_reason) IS DISTINCT FROM
            ROW(ds2.amount_value, ds2.amount_unit_concept_id, ds2.numerator_value, ds2.numerator_unit_concept_id, ds2.denominator_value, ds2.denominator_unit_concept_id, ds2.box_size, ds2.valid_start_date, ds2.valid_end_date, ds2.invalid_reason);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_concept_class_delta(
    pVersion1 integer,
    pVersion2 integer,
    pVocabularies text[],
    pCsvView boolean
)
    RETURNS TABLE (
                      row_change_type            text,
                      attribute_modified         text,
                      concept_class_id           varchar(20),
                      concept_class_name         varchar(255),
                      concept_class_concept_id   numeric(38)
                  )
AS $$
BEGIN
    IF pVersion1 IS NULL OR pVersion2 IS NULL THEN
        RETURN;
    END IF;
    RETURN QUERY
        SELECT
            CASE
                WHEN cc2.concept_class_id IS NULL THEN 'I'
                WHEN cc1.concept_class_id IS NULL THEN 'D'
                ELSE 'U'
                END AS row_change_type,
            CASE
                WHEN pCsvView AND cc1.concept_class_id IS NOT NULL AND cc2.concept_class_id IS NOT NULL THEN
                    CONCAT_WS(', ',
                              CASE WHEN cc1.concept_class_name IS DISTINCT FROM cc2.concept_class_name THEN 'concept_class_name' END,
                              CASE WHEN cc1.concept_class_concept_id IS DISTINCT FROM cc2.concept_class_concept_id THEN 'concept_class_concept_id' END
                        )
                END AS attribute_modified,
            COALESCE(cc1.concept_class_id, cc2.concept_class_id) AS concept_class_id,
            cc1.concept_class_name,
            cc1.concept_class_concept_id
        FROM
            (SELECT * FROM concept_class_history a1 WHERE a1.version = pVersion1) cc1
                FULL JOIN
            (SELECT * FROM concept_class_history a2 WHERE a2.version = pVersion2) cc2
            USING (concept_class_id)
        WHERE
            ROW(cc1.concept_class_name, cc1.concept_class_concept_id) IS DISTINCT FROM
            ROW(cc2.concept_class_name, cc2.concept_class_concept_id);
END;
$$ LANGUAGE plpgsql;
