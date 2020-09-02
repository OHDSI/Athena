CPT4 utility for CDM v5.

This utility will import the CPT4 vocabulary into concept.csv.
Internet connection is required.

Start import process from command line with:
 windows: cpt.bat APIKEY
      or: cpt.bat USER PASSWORD

 linux: ./cpt.sh APIKEY
    or: ./cpt.sh USER PASSWORD
Use USER/PASSWORD from UMLS account: https://utslogin.nlm.nih.gov/cas/login.
Or API KEY from UMLS account profile: https://uts.nlm.nih.gov//uts.html#profile
Do not close or shutdown your PC until the end of import process,
it will cause damage to concept.csv file.

Please make sure java allowed to make http requests and has write permission to the concept.csv file.
If your username or password contains special symbols or space please wrap it with single quotes in linux or double quotes in windows e.g. "user$1@email" or 'pa$$word'.