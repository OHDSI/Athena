DROP MATERIALIZED VIEW concepts_view CASCADE;

CREATE MATERIALIZED VIEW concepts_view AS

  SELECT c.concept_id,
         c.concept_name,
         c.domain_id,
         c.vocabulary_id,
         c.concept_class_id,
         c.concept_code,
         c.valid_start_date,
         c.valid_end_date,
         CASE c.invalid_reason WHEN 'U' THEN 'Invalid'
                               WHEN 'D' THEN 'Invalid'
                               ELSE 'Valid'
         END AS invalid_reason,

         CASE c.standard_concept WHEN 'C' THEN 'Classification'
                                 WHEN 'S' THEN 'Standard'
                                 ELSE 'Non-standard'
         END AS standard_concept,

         string_agg(concept_synonym_name, ' ') AS concept_synonym_name

  FROM concept c
  LEFT JOIN concept_synonym cs on cs.concept_id = c.concept_id
  GROUP BY c.concept_id,
    c.concept_name,
    c.domain_id,
    c.vocabulary_id,
    c.concept_class_id,
    c.concept_code,
    c.valid_start_date,
    c.valid_end_date,
    c.invalid_reason,
    c.standard_concept
;
CREATE INDEX concepts_view_concept_id_ind ON concepts_view (concept_id);