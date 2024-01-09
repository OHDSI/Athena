CREATE OR REPLACE FUNCTION get_sql_statements_delta(
    pVersion1 INTEGER,
    pVersion2 INTEGER,
    pVocabularies TEXT[]
)
    RETURNS TABLE (script_text TEXT)
AS $$
BEGIN
    RETURN QUERY
        SELECT
            CASE WHEN row_change_type = 'I' THEN FORMAT('INSERT INTO concept (concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason, concept_id) VALUES (%L,%L,%L,%L,%L,%L,%L,%L,%L,%s);',
                                                        concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason, concept_id)
                 WHEN row_change_type = 'U' THEN FORMAT('UPDATE concept SET  (concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason) = (%L,%L,%L,%L,%L,%L,%L,%L,%L) WHERE concept_id=%s;',
                                                        concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason, concept_id)
                 WHEN row_change_type = 'D' THEN FORMAT('DELETE FROM concept WHERE concept_id=%s;', concept_id)
                END script_text
        FROM get_concept_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT('INSERT INTO concept_ancestor (min_levels_of_separation, max_levels_of_separation, ancestor_vocabulary_id, descendant_vocabulary_id, ancestor_concept_id, descendant_concept_id) VALUES (%s, %s, %s, %s, %L, %L);',
                                                       min_levels_of_separation, max_levels_of_separation, ancestor_vocabulary_id, descendant_vocabulary_id, ancestor_concept_id, descendant_concept_id)
                WHEN row_change_type = 'U' THEN FORMAT('UPDATE concept_ancestor SET  (min_levels_of_separation, max_levels_of_separation, ancestor_vocabulary_id, descendant_vocabulary_id) = (%s, %s, %L, %L) WHERE ancestor_concept_id = %s AND descendant_concept_id = %s;',
                                                       min_levels_of_separation, max_levels_of_separation, ancestor_vocabulary_id, descendant_vocabulary_id, ancestor_concept_id, descendant_concept_id)
                WHEN row_change_type = 'D' THEN FORMAT('DELETE FROM concept_ancestor WHERE ancestor_concept_id = %s AND descendant_concept_id = %s;', ancestor_concept_id, descendant_concept_id)
                END AS script_text
        FROM get_concept_ancestor_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_relationship (valid_start_date, valid_end_date, invalid_reason, vocabulary_id_1, vocabulary_id_2, concept_id_1, concept_id_2, relationship_id) VALUES (%L, %L, %L, %L, %L, %s, %s, %L);',
                                                        valid_start_date, valid_end_date, invalid_reason, vocabulary_id_1, vocabulary_id_2, concept_id_1, concept_id_2, relationship_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE concept_relationship SET  (valid_start_date, valid_end_date, invalid_reason) = (%L, %L, %L) WHERE concept_id_1 = %s AND concept_id_2 = %s AND relationship_id = %L;',
                                                        valid_start_date, valid_end_date, invalid_reason, concept_id_1, concept_id_2, relationship_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_relationship WHERE concept_id_1 = %s AND concept_id_2 = %s AND relationship_id = %L;', concept_id_1, concept_id_2, relationship_id)
                END AS script_text
        FROM get_concept_relationship_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_synonym (concept_synonym_name, language_concept_id, vocabulary_id, concept_id) VALUES (%L, %s, %L, %s);',
                                                        concept_synonym_name, language_concept_id, vocabulary_id, concept_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE concept_synonym SET (concept_synonym_name, language_concept_id, vocabulary_id) = (%L, %s, %L) WHERE concept_id = %s;',
                                                        concept_synonym_name, language_concept_id, vocabulary_id, concept_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_synonym WHERE concept_id = %s;', concept_id)
                END AS script_text

        FROM get_concept_synonym_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO domain (domain_name, domain_concept_id, domain_id) VALUES (%L, %s, %L);',
                                                        domain_name, domain_concept_id, domain_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE domain SET  (domain_name, domain_concept_id) = (%L, %s) WHERE domain_id = %L;',
                                                        domain_name, domain_concept_id, domain_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM domain WHERE domain_id = %L;', domain_id)
                END AS script_text
        FROM get_domain_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO drug_strength (amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, vocabulary_id, drug_concept_id, ingredient_concept_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %L, %L, %L, %s);',
                                                        amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, vocabulary_id, drug_concept_id, ingredient_concept_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE drug_strength SET  (amount_value,   amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, ingredient_concept_id) = (%s, %s, %s, %s, %s, %s, %s, %L, %L, %L, %s) WHERE drug_concept_id = %s;',
                                                        amount_value, amount_unit_concept_id, numerator_value, numerator_unit_concept_id, denominator_value, denominator_unit_concept_id, box_size, valid_start_date, valid_end_date, invalid_reason, ingredient_concept_id, drug_concept_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM drug_strength WHERE drug_concept_id = %s;', drug_concept_id)
                END AS script_text
        FROM get_drug_strength_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO relationship (relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id, relationship_id) VALUES (%L, %L, %L, %L, %s, %L);',
                                                        relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id, relationship_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE relationship SET  (relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id) = (%L, %L, %L, %L, %s) WHERE relationship_id = %L;',
                                                        relationship_name, is_hierarchical, defines_ancestry, reverse_relationship_id, relationship_concept_id, relationship_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM relationship WHERE relationship_id = %L;', relationship_id)
                END AS script_text
        FROM get_relationship_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO vocabulary (vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id) VALUES (%L, %L, %L, %s, %L);',
                                                        vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE vocabulary SET ( vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id) = (%L, %L, %L, %s) WHERE vocabulary_id = %L;',
                                                        vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id, vocabulary_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM vocabulary WHERE vocabulary_id = %L;', vocabulary_id)
                END AS script_text
        FROM get_vocabulary_delta(pVersion1, pVersion2, pVocabularies, false)
        UNION ALL
        SELECT
            CASE
                WHEN row_change_type = 'I' THEN FORMAT ('INSERT INTO concept_class (concept_class_name, concept_class_concept_id, version, concept_class_id) VALUES (%L, %s, %s, %L);',
                                                        concept_class_name, concept_class_concept_id, version, concept_class_id)
                WHEN row_change_type = 'U' THEN FORMAT ('UPDATE concept_class SET  (concept_class_name, concept_class_concept_id, version) = (%L, %s, %s) WHERE concept_class_id = %L;',
                                                        concept_class_name, concept_class_concept_id, version, concept_class_id)
                WHEN row_change_type = 'D' THEN FORMAT ('DELETE FROM concept_class WHERE concept_class_id = %L;', concept_class_id)
                END AS script_text
        FROM get_concept_class_delta(pVersion1, pVersion2, pVocabularies, false);
END;
$$ LANGUAGE plpgsql