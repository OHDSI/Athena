CREATE VIEW concept_relationships_view AS
  SELECT
    cr.relationship_id  AS relationship_id,
    r.relationship_name AS relationship_name,

    sc.concept_id       AS source_concept_id,
    sc.standard_concept AS source_standard_concept,

    tc.concept_id       AS target_concept_id,
    tc.concept_name     AS target_concept_name,
    tc.vocabulary_id    AS target_concept_vocabulary_id
  FROM concept_relationship cr
    JOIN concept sc ON sc.concept_id = cr.concept_id_1
    JOIN concept tc ON tc.concept_id = cr.concept_id_2
    JOIN relationship r ON r.relationship_id = cr.relationship_id
  WHERE CURRENT_DATE BETWEEN cr.valid_start_date AND cr.valid_end_date;