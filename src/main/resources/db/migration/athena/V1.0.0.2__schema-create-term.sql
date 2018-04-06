CREATE TABLE term (
  id BIGINT PRIMARY KEY NOT NULL,
  domain	VARCHAR(255) NULL,
  vocabulary	VARCHAR(255)	NOT NULL,
  concept_id BIGINT NOT NULL,
  concept_name VARCHAR(255) NOT NULL,
  concept_class VARCHAR(255) NOT NULL,
  concept_code VARCHAR(255) NOT NULL,
  standard_concept VARCHAR(255) NOT NULL,
  invalid_reason VARCHAR(255) NOT NULL
);

INSERT INTO term(
  id, domain, vocabulary, concept_id, concept_name,
  concept_class, concept_code, standard_concept, invalid_reason)
VALUES (100500,
        'Drug',
        'RxNorm Extension',
        43143682,
        '1020 MG Aspirin 0.489 MG/MG Powder for Oral Solution',
        'Quant Clinical Drug',
        'OMOP432277', 'Standard', 'Valid');
