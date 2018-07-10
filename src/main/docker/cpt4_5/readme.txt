CPT4 utility for CDM v5.

This utility will import the CPT4 vocabulary into concept.csv.
Internet connection is required.

Start import process from command line with: "java -Dumls-user=xxx -Dumls-password=xxx -jar cpt4.jar 5"
or use cpt.sh or cpt.bat depending on your OS. Please replace "xxx" with UMLS username and password (https://utslogin.nlm.nih.gov/cas/login).
Do not close or shutdown your PC until the end of import process,
it will cause damage to concept.csv file.

The number of imported records will be shown in command line,
import process finishes with: "CPT successfully updated." message.