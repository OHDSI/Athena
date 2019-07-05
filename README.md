#**Launching tests:**

1. Create databases:\
    `CREATE DATABASE athena_db_test OWNER ohdsi;`\
    `GRANT ALL PRIVILEGES ON DATABASE athena_db_test TO ohdsi;`
      
    `CREATE DATABASE athena_cdm_v5_test OWNER ohdsi;`\
    `GRANT ALL PRIVILEGES ON DATABASE athena_cdm_v5_test TO ohdsi;`

2. Launch any test from `service.concept.nonexact` folder so Flyway migrations will pass

3. Load test data:\
    `COPY DRUG_STRENGTH FROM 'C:\projects\Athena\src\test\resources\testdata\DRUG_STRENGTH.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY CONCEPT FROM 'C:\projects\Athena\src\test\resources\testdata\CONCEPT.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY CONCEPT_RELATIONSHIP FROM 'C:\projects\Athena\src\test\resources\testdata\CONCEPT_RELATIONSHIP.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY CONCEPT_ANCESTOR FROM 'C:\projects\Athena\src\test\resources\testdata\CONCEPT_ANCESTOR.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY CONCEPT_SYNONYM FROM 'C:\projects\Athena\src\test\resources\testdata\CONCEPT_SYNONYM.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY VOCABULARY FROM 'C:\projects\Athena\src\test\resources\testdata\VOCABULARY.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY RELATIONSHIP FROM 'C:\projects\Athena\src\test\resources\testdata\RELATIONSHIP.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY CONCEPT_CLASS FROM 'C:\projects\Athena\src\test\resources\testdata\CONCEPT_CLASS.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`\
    `COPY DOMAIN FROM 'C:\projects\Athena\src\test\resources\testdata\DOMAIN.csv' WITH DELIMITER E'\t' CSV HEADER QUOTE E'\b' ;`

    `REFRESH MATERIALIZED VIEW concepts_view;`

4. Start SOLR:
    + in `solrconfig.xml` in `<requestHandler name="/dataimport" class="solr.DataImportHandler">` section change 
   `<str name="url">jdbc:postgresql://localhost:5432/athena_cdm_v5</str>` to `<str name="url">jdbc:postgresql://localhost:5432/athena_cdm_v5_test</str>`
   
    + Launch reindex via web-console
    + Restart SOLR container if it is running in Docker
   
5. Launch tests