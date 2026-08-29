-- Teach the vocabulary loader about the three CDM 5.5 tables.
--
-- Both functions are reproduced in full because plpgsql has no way to amend one in place.
-- The only changes against V20170928141101 and V20171221202125 are the three import_table
-- calls, the six index statements and the three locks.
--
-- import_table() derives the source file from the prefix and the table name, so these
-- load from /home/athena_vocab_loader/v5_concept_metadata.csv and its two siblings.

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
  DROP INDEX CONCEPT_METADATA_CONCEPTID;
  DROP INDEX CONCEPT_RELATIONSHIP_METADATA_C_1;
  DROP INDEX CONCEPT_RELATIONSHIP_METADATA_C_2;
  DROP INDEX PACK_CONCEPTID;
  DROP INDEX PACK_DRUG_CONCEPTID;

  PERFORM import_table(db, csv_prefix, 'concept');
  PERFORM import_table(db, csv_prefix, 'concept_relationship');
  PERFORM import_table(db, csv_prefix, 'concept_synonym');

  PERFORM import_table(db, csv_prefix, 'concept_ancestor');
  PERFORM import_table(db, csv_prefix, 'relationship');
  PERFORM import_table(db, csv_prefix, 'vocabulary');

  PERFORM import_table(db, csv_prefix, 'concept_class');
  PERFORM import_table(db, csv_prefix, 'domain');
  PERFORM import_table(db, csv_prefix, 'drug_strength');

  PERFORM import_table(db, csv_prefix, 'concept_metadata');
  PERFORM import_table(db, csv_prefix, 'concept_relationship_metadata');
  PERFORM import_table(db, csv_prefix, 'pack_content');

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

  CREATE INDEX CONCEPT_METADATA_CONCEPTID
    ON CONCEPT_METADATA (CONCEPT_ID);

  CREATE INDEX CONCEPT_RELATIONSHIP_METADATA_C_1
    ON CONCEPT_RELATIONSHIP_METADATA (CONCEPT_ID_1);
  CREATE INDEX CONCEPT_RELATIONSHIP_METADATA_C_2
    ON CONCEPT_RELATIONSHIP_METADATA (CONCEPT_ID_2);

  CREATE INDEX PACK_CONCEPTID
    ON PACK_CONTENT (PACK_CONCEPT_ID);
  CREATE INDEX PACK_DRUG_CONCEPTID
    ON PACK_CONTENT (DRUG_CONCEPT_ID);

END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;


CREATE OR REPLACE FUNCTION safe_import_of_tables(
)
  RETURNS VOID AS
$body$
DECLARE
  db         VARCHAR := 'athena_cdm_v5';
BEGIN

  RAISE NOTICE '%: safe_import_of_tables is started: %', db, now();

  PERFORM cancel_queries_except_current(db);

  RAISE NOTICE '%: lock tables is started', db;

  LOCK TABLE concept IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_relationship IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_synonym IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_ancestor IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE relationship IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE vocabulary IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_class IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE "domain" IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE drug_strength IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_metadata IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE concept_relationship_metadata IN ACCESS EXCLUSIVE MODE;
  LOCK TABLE pack_content IN ACCESS EXCLUSIVE MODE;

  PERFORM import_tables();

  REFRESH MATERIALIZED VIEW concepts_view;
  RAISE NOTICE '%: safe_import_of_tables() is finished', db;
END;
$body$
LANGUAGE 'plpgsql'
SECURITY INVOKER
COST 100;
