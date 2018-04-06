CREATE OR REPLACE FUNCTION import_tables(
)
  RETURNS VOID AS
$body$
DECLARE
  db         VARCHAR := 'athena_cdm_v5';
  csv_prefix VARCHAR := 'v5_';
BEGIN
  RAISE NOTICE '%: import_tables is started: %', db, now();

  DROP INDEX DRUG_CONCEPTID;
  DROP INDEX INGREDIENT_CONCEPTID;
  DROP INDEX CONCEPT_RELATIONSHIP_C_1;
  DROP INDEX CONCEPT_RELATIONSHIP_C_2;
  DROP INDEX ancestor_conceptid;
  DROP INDEX descendant_conceptid;
  DROP INDEX concept_ancestor_desc_concept_level;
  DROP INDEX concept_vocab;

  PERFORM import_table(db, csv_prefix, 'concept');
  PERFORM import_table(db, csv_prefix, 'concept_relationship');
  PERFORM import_table(db, csv_prefix, 'concept_synonym');

  PERFORM import_table(db, csv_prefix, 'concept_ancestor');
  PERFORM import_table(db, csv_prefix, 'relationship');
  PERFORM import_table(db, csv_prefix, 'vocabulary');

  PERFORM import_table(db, csv_prefix, 'concept_class');
  PERFORM import_table(db, csv_prefix, 'domain');
  PERFORM import_table(db, csv_prefix, 'drug_strength');

  RAISE NOTICE 'restore INDEXES';
  CREATE INDEX DRUG_CONCEPTID
    ON DRUG_STRENGTH (DRUG_CONCEPT_ID);
  CREATE INDEX INGREDIENT_CONCEPTID
    ON DRUG_STRENGTH (INGREDIENT_CONCEPT_ID);

  CREATE INDEX CONCEPT_RELATIONSHIP_C_1
    ON CONCEPT_RELATIONSHIP (CONCEPT_ID_1);
  CREATE INDEX CONCEPT_RELATIONSHIP_C_2
    ON CONCEPT_RELATIONSHIP (CONCEPT_ID_2);

  CREATE INDEX ANCESTOR_CONCEPTID
    ON CONCEPT_ANCESTOR (ANCESTOR_CONCEPT_ID);
  CREATE INDEX DESCENDANT_CONCEPTID
    ON CONCEPT_ANCESTOR (DESCENDANT_CONCEPT_ID);
  CREATE INDEX concept_ancestor_desc_concept_level
    ON concept_ancestor (descendant_concept_id, min_levels_of_separation);

  CREATE INDEX CONCEPT_VOCAB
    ON CONCEPT (VOCABULARY_ID);

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;