## Requirements
### [docker-compose](https://github.com/docker/compose): v1.28.0+

### Vocabulary Files
The vocabulary files needed for the data import scripts to run must be in `csv` format.
If you've exported data from [Athena](https://athena.ohdsi.org/), the files default to `tsv` format.

```shell
➜ tree

.
├── CONCEPT.csv
├── CONCEPT_ANCESTOR.csv
├── CONCEPT_CLASS.csv
├── CONCEPT_RELATIONSHIP.csv
├── CONCEPT_SYNONYM.csv
├── DOMAIN.csv
├── DRUG_STRENGTH.csv
├── RELATIONSHIP.csv
└── VOCABULARY.csv
```

As such, a bit of massaging is required to get the data into the correct format:
```zsh
for i in `ls`; do head -n 100 $i | jq --raw-input --raw-output --slurp 'split("\n") | map(split("\t")) | .[] | select(length > 0) | @csv' > ../vocabulary/v5_${i:l}; done
```

```shell
➜ tree

.
├── v5_concept.csv
├── v5_concept_ancestor.csv
├── v5_concept_class.csv
├── v5_concept_relationship.csv
├── v5_concept_synonym.csv
├── v5_domain.csv
├── v5_drug_strength.csv
├── v5_relationship.csv
└── v5_vocabulary.csv
```

The `docker-compose` volume mount assumes there is a `vocabulary` directory at the root of the project to run the `safe_import_of_tables` function.

---

## Runbook

### Maven Build
```shell
# chicken-egg paradigm: 
# some of the unit tests depend on a running Postgres instance, so we are skipping the unit tests here
mvn clean package -Dmaven.test.skip
```

### Docker Build
```shell
mvn docker:build
```

### Docker Compose Up
```shell
docker-compose up
```

### Import Vocabulary Tables
```shell
docker-compose run import 'select safe_import_of_tables()'
```

### Import Solr Concepts
```shell
docker-compose exec solr bash -c \
"echo '{ synchronous: true }' | post -url \"http://solr:8983/solr/concepts/dataimport?command=full-import\" -out yes -d"
```
