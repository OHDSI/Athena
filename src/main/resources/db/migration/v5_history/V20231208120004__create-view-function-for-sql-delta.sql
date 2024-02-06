CREATE OR REPLACE FUNCTION get_sql_statements_delta(
    pVersion1 INTEGER,
    pVersion2 INTEGER,
    pVocabularies TEXT[]
)
    RETURNS TABLE (script_text TEXT)
AS $$
DECLARE


    -- Constants for Insert/Update Operations (I/U)
    CONCEPT_DICT            CONSTANT INTEGER := 110;
    CONCEPT_CLASS           CONSTANT INTEGER := 120;
    DOMAIN                  CONSTANT INTEGER := 130;
    RELATIONSHIP            CONSTANT INTEGER := 140;
    RELATIONSHIP_U_FOR_I    CONSTANT INTEGER := 145;
    VOCABULARY              CONSTANT INTEGER := 150;
    CONCEPT                 CONSTANT INTEGER := 160;
    CONCEPT_ANCESTOR        CONSTANT INTEGER := 170;
    CONCEPT_RELATIONSHIP    CONSTANT INTEGER := 180;
    CONCEPT_SYNONYM         CONSTANT INTEGER := 190;
    DRUG_STRENGTH           CONSTANT INTEGER := 200;

    -- Constants for Delete Operations (D)
    CONCEPT_ANCESTOR_D      CONSTANT INTEGER := 210;
    CONCEPT_RELATIONSHIP_D  CONSTANT INTEGER := 220;
    CONCEPT_SYNONYM_D       CONSTANT INTEGER := 230;
    DRUG_STRENGTH_D         CONSTANT INTEGER := 240;
    CONCEPT_D               CONSTANT INTEGER := 250;
    CONCEPT_CLASS_D         CONSTANT INTEGER := 260;
    DOMAIN_D                CONSTANT INTEGER := 270;
    RELATIONSHIP_U_FOR_D    CONSTANT INTEGER := 275;
    RELATIONSHIP_D          CONSTANT INTEGER := 280;
    VOCABULARY_D            CONSTANT INTEGER := 290;
    CONCEPT_DICT_D          CONSTANT INTEGER := 300;
    CONCEPT_U               CONSTANT INTEGER := 310;

    -- This mock value helps overcome the inactive idx_unique_concept_code constraint in the code. We can set it initially and update with proper values later.
    MOCK_CODE               CONSTANT VARCHAR= 'OMOP generated';
BEGIN
    RETURN QUERY
    WITH ConceptScript AS (
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT('INSERT INTO concept (concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason, concept_id) VALUES ' ||
                                                                            '(%L, %L, %L, %L, %L, %L, %L, %L, %L, %s);',
                                                                             concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, MOCK_CODE,    valid_start_date, valid_end_date, invalid_reason, concept_id)
                WHEN row_change_type = 'U' THEN FORMAT('UPDATE concept SET  (concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason) = ' ||
                                                                           '(%L, %L, %L, %L, %L, %L, %L, %L, %L) WHERE concept_id=%s;',
                                                                             concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, MOCK_CODE,    valid_start_date, valid_end_date, invalid_reason, concept_id)
                WHEN row_change_type = 'D' THEN FORMAT('DELETE FROM concept WHERE concept_id=%s;', concept_id)

                END AS script_text,
            row_change_type,
            concept_class_id
        FROM get_concept_delta(pVersion1, pVersion2, pVocabularies, false)
        ),
        ConceptUpdateScript AS (
            SELECT FORMAT('UPDATE concept SET concept_code = %L WHERE concept_id=%s;', concept_code, concept_id) AS script_text
            FROM get_concept_delta(pVersion1, pVersion2, pVocabularies, false)
            WHERE row_change_type IN ('I','U')
        ),
         RelationshipScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO relationship (relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id, relationship_id) VALUES ' ||
                                                                                      '(%L, %L, %L, %L, %s, %L);',
                                                                                        relationship_name, is_hierarchical, defines_ancestry, relationship_id, relationship_concept_id, relationship_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE relationship SET  (relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id) = ' ||
                                                                                      '(%L, %L, %L, %L, %s) WHERE relationship_id = %L;',
                                                                                        relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id,    relationship_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM relationship WHERE relationship_id = %L;', relationship_id)
                     END AS script_text,
                 row_change_type
             FROM get_relationship_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         --We cannot directly set the reverse_relationship_id for a recursive relationship.
        -- All relationships should be added first. Only after that, the proper reverse_relationship_id can be set.
         RelationshipUpdateForInsertScript AS (
             SELECT
                 FORMAT ('UPDATE relationship SET reverse_relationship_id = %L WHERE relationship_id = %L;', reverse_relationship_id, relationship_id)
                  script_text
             FROM get_relationship_delta(pVersion1, pVersion2, pVocabularies, false) r
             WHERE r.row_change_type = 'I'
         ),

         RelationshipUpdateForDeleteScript AS (
             SELECT
                 FORMAT ('UPDATE relationship SET reverse_relationship_id = %L WHERE relationship_id = %L;', relationship_id, relationship_id)
                     script_text
             FROM get_relationship_delta(pVersion1, pVersion2, pVocabularies, false) r
             WHERE r.row_change_type = 'D'
         ),

         ConceptAncestorScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT('INSERT INTO concept_ancestor (min_levels_of_separation, max_levels_of_separation,  ancestor_concept_id, descendant_concept_id) VALUES ' ||
                                                                                         '(%s, %s, %L, %L);',
                                                                                           min_levels_of_separation, max_levels_of_separation,  ancestor_concept_id, descendant_concept_id)
                     WHEN row_change_type = 'U' THEN FORMAT('UPDATE concept_ancestor SET  (min_levels_of_separation, max_levels_of_separation) = ' ||
                                                                                          '(%s, %s) WHERE ancestor_concept_id = %s AND descendant_concept_id = %s;',
                                                                                           min_levels_of_separation, max_levels_of_separation, ancestor_concept_id, descendant_concept_id)
                     WHEN row_change_type = 'D' THEN FORMAT('DELETE FROM concept_ancestor WHERE ancestor_concept_id = %s AND descendant_concept_id = %s;', ancestor_concept_id, descendant_concept_id)
                     END AS script_text,
                 row_change_type
             FROM get_concept_ancestor_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         ConceptRelationshipScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_relationship (valid_start_date, valid_end_date, invalid_reason, concept_id_1, concept_id_2, relationship_id) VALUES (%L, %L, %L, %s, %s, %L);',
                                                                                                valid_start_date, valid_end_date, invalid_reason, concept_id_1, concept_id_2, relationship_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE concept_relationship SET  (valid_start_date, valid_end_date, invalid_reason) = (%L, %L, %L) WHERE concept_id_1 = %s AND concept_id_2 = %s AND relationship_id = %L;',
                                                                                                valid_start_date, valid_end_date, invalid_reason, concept_id_1, concept_id_2, relationship_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_relationship WHERE concept_id_1 = %s AND concept_id_2 = %s AND relationship_id = %L;', concept_id_1, concept_id_2, relationship_id)
                     END AS script_text,
                 row_change_type
             FROM get_concept_relationship_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         ConceptSynonymScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_synonym (concept_synonym_name, language_concept_id, concept_id) VALUES (%L, %s, %s);',
                                                             concept_synonym_name, language_concept_id, concept_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_synonym WHERE concept_id = %s AND concept_synonym_name = %L AND language_concept_id = %L;', concept_id, concept_synonym_name, language_concept_id)
                     END AS script_text,
                 row_change_type
             FROM get_concept_synonym_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         DomainScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO domain (domain_name, domain_concept_id, domain_id) VALUES (%L, %s, %L);',
                                                             domain_name, domain_concept_id, domain_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE domain SET  (domain_name, domain_concept_id) = (%L, %s) WHERE domain_id = %L;',
                                                             domain_name, domain_concept_id, domain_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM domain WHERE domain_id = %L;', domain_id)
                     END AS script_text,
                 row_change_type
             FROM get_domain_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         DrugStrengthScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO drug_strength (amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, drug_concept_id, ingredient_concept_id) VALUES ' ||
                                                             '(%L, %L, %L, %L, %L, %L, %L, %L, %L, %L, %L, %L);',
                                                             amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, drug_concept_id, ingredient_concept_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE drug_strength SET  (amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, ingredient_concept_id) = ' ||
                                                             '(%L, %L, %L, %L, %L, %L, %L, %L, %L, %L, %L) WHERE drug_concept_id = %s AND ingredient_concept_id = %s;',
                                                             amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, ingredient_concept_id, drug_concept_id, ingredient_concept_id)
                     WHEN row_change_type = 'D' THEN
                         FORMAT ('DELETE FROM drug_strength WHERE drug_concept_id = %s AND ingredient_concept_id = %s;', drug_concept_id, ingredient_concept_id)
                     END AS script_text,
                 row_change_type
             FROM get_drug_strength_delta(pVersion1, pVersion2, pVocabularies, false)
         ),

         VocabularyScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO vocabulary (vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id) VALUES (%L, %L, %L, %s, %L);',
                                                             vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE vocabulary SET ( vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id) = (%L, %L, %L, %s) WHERE vocabulary_id = %L;',
                                                             vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM vocabulary WHERE vocabulary_id = %L;', vocabulary_id)
                     END AS script_text,
                 row_change_type
             FROM get_vocabulary_delta(pVersion1, pVersion2, pVocabularies, false)
         ),
         ConceptClassScript AS (
             SELECT
                 CASE
                     WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_class (concept_class_name, concept_class_concept_id, concept_class_id) VALUES (%L, %s, %L);',
                                                             concept_class_name, concept_class_concept_id,  concept_class_id)
                     WHEN row_change_type = 'U' THEN FORMAT ('UPDATE concept_class SET  (concept_class_name, concept_class_concept_id) = (%L, %s) WHERE concept_class_id = %L;',
                                                             concept_class_name, concept_class_concept_id,  concept_class_id)
                     WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_class WHERE concept_class_id = %L;', concept_class_id)
                     END AS script_text,
                 row_change_type
             FROM get_concept_class_delta(pVersion1, pVersion2, pVocabularies, false)
         )

    SELECT sqls.script_text
        FROM (
                 SELECT
                     CASE WHEN cs.row_change_type = 'D' AND cs.concept_class_id IN ('Concept Class', 'Domain', 'Relationship', 'Vocabulary')
                            THEN CONCEPT_DICT_D
                          WHEN cs.row_change_type = 'D'
                            THEN CONCEPT_D
                          WHEN cs.concept_class_id IN ('Concept Class', 'Domain', 'Relationship', 'Vocabulary')
                            THEN CONCEPT_DICT
                            ELSE CONCEPT
                     END AS script_number, cs.script_text
                 FROM ConceptScript cs
                 UNION ALL
                 SELECT
                     CASE WHEN ccs.row_change_type = 'D'
                            THEN CONCEPT_CLASS_D
                            ELSE CONCEPT_CLASS
                     END AS script_number, ccs.script_text
                 FROM ConceptClassScript ccs
                 UNION ALL
                 SELECT
                     CASE WHEN ds.row_change_type = 'D'
                             THEN DOMAIN_D
                             ELSE DOMAIN
                     END AS script_number, ds.script_text
                 FROM DomainScript ds
                 UNION ALL
                 SELECT
                     CASE WHEN rs.row_change_type = 'D'
                             THEN RELATIONSHIP_D
                             ELSE RELATIONSHIP
                     END AS script_number, rs.script_text
                 FROM RelationshipScript rs
                 UNION ALL
                 SELECT RELATIONSHIP_U_FOR_I
                     AS script_number, rs.script_text
                 FROM RelationshipUpdateForInsertScript rs
                 UNION ALL
                 SELECT RELATIONSHIP_U_FOR_D
                     AS script_number, rs.script_text
                 FROM RelationshipUpdateForDeleteScript rs
                 UNION ALL
                 SELECT
                     CASE WHEN vs.row_change_type = 'D'
                            THEN VOCABULARY_D
                            ELSE VOCABULARY
                     END AS script_number, vs.script_text
                 FROM VocabularyScript vs
                 UNION ALL
                 SELECT
                     CASE WHEN cas.row_change_type = 'D'
                            THEN CONCEPT_ANCESTOR_D
                            ELSE CONCEPT_ANCESTOR
                     END AS script_number, cas.script_text
                 FROM ConceptAncestorScript cas
                 UNION ALL
                 SELECT
                     CASE WHEN crs.row_change_type = 'D'
                            THEN CONCEPT_RELATIONSHIP_D
                            ELSE CONCEPT_RELATIONSHIP
                     END AS script_number, crs.script_text
                 FROM ConceptRelationshipScript crs
                 UNION ALL
                 SELECT
                     CASE WHEN css.row_change_type = 'D'
                            THEN CONCEPT_SYNONYM_D
                            ELSE CONCEPT_SYNONYM
                     END AS script_number, css.script_text
                 FROM ConceptSynonymScript css
                 UNION ALL
                 SELECT
                     CASE WHEN dss.row_change_type = 'D'
                            THEN DRUG_STRENGTH_D
                            ELSE DRUG_STRENGTH
                     END AS script_number, dss.script_text
                 FROM DrugStrengthScript dss
                 UNION ALL
                 SELECT
                     CONCEPT_U script_number,
                     cus.script_text
                 FROM ConceptUpdateScript cus
             ) as sqls
        ORDER BY sqls.script_number;
END;
$$ LANGUAGE plpgsql;
