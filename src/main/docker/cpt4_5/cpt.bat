@echo OFF
IF NOT "%1"=="" IF NOT "%2"=="" GOTO PWD
IF NOT "%1"=="" IF "%2"=="" GOTO APIKEY
ECHO Please specify UMLS APIKEY or USERNAME and PASSWORD
GOTO EOF

:APIKEY
java -Dumls-apikey=%1 -jar cpt4.jar 5
GOTO EOF

:PWD
java -Dumls-user=%1 -Dumls-password=%2 -jar cpt4.jar 5

:EOF