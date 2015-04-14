This utility allows to import the CPT4 dictionary from UMLS webAPI.
You must have an account at http://www.nlm.nih.gov/research/umls/ its requisites are used
for webAPI authorization.

The application should be started as usual .jar application:
java -jar cpt4.jar -version -login -password -path_to_concept_cpt4.csv -path_to_concept.csv

The arguments list:
- version: vocabulary version 4 or 5
- login: username for http://www.nlm.nih.gov/research/umls/
- password: password for http://www.nlm.nih.gov/research/umls/
- path_to_concept_cpt4.csv: full path to concept_cpt4.csv
- path_to_concept.csv: full path to concept.csv