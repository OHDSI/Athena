These instructions can be used to setup a local development environment to build and execute Athena. The software relies on a postgresql database and a SOLR indexing server and instructions are provided for installing both in your local environment. 

## DevTools - Java, Maven, git

You will need at least [OpenJDK 8u282](https://developers.redhat.com/products/openjdk/download), [Maven 4.0.0](https://maven.apache.org/download.cgi) and [git](https://git-scm.com/downloads), and [Docker](https://www.docker.com) to obtain the Athena codebase and build it. Additionally, you will need an instance of [Postgresql 12+](https://www.postgresql.org/download/) and an instance of [SOLR 7.7+](https://solr.apache.org/downloads.html) to execute the tests and operate the runtime environment. Setup for those is described below. Part of the SOLR installation will require you to also obtain the [Postgresql JDBC driver](https://jdbc.postgresql.org/download.html). It is also helpful to have access to [curl](https://curl.se/), but it isn't strictly necessary.
        
## PostGresql version 12

After installing postgresql, connect to the database server as an administrator and execute the following SQL.
    
```sql
create user ohdsi with login nosuperuser inherit nocreatedb nocreaterole noreplication PASSWORD 'ohdsi';
grant pg_read_server_files to ohdsi;
create database athena_db owner ohdsi;
create database athena_db_test owner ohdsi;
create database athena_cdm_v4_5 owner ohdsi;
create database athena_cdm_v5 owner ohdsi;
create database athena_cdm_v5_test owner ohdsi;
```

## SOLR 7.7

After obtaining the SOLR distribution and Postgresql JDBC driver, run the following commands to create a basic instance of SOLR with the appropriate collection definition for the Athena concepts index. The `-m 2g` memory setting may be adjusted as needed to fit your environment, but needs to be larger than the 512m heap that SOLR uses by default.
    
```bash
unzip solr-7.7.*.zip
cd solr-7.7.*
./bin/solr start
./bin/solr create -c concepts
./bin/solr stop -p 8983
cp ~/git/Athena/src/main/resources/solr/* server/solr/concepts/conf
mkdir server/solr/concepts/lib
cp postgresql-42.*.jar server/solr/concepts/lib
./bin/solr start -p 8984 -m 2g
```

## Athena
```bash
cd ~/git
git clone git@github.com:OHDSI/Athena.git
cd Athena
mvn clean install
java -jar target/athena.jar
```

If you chose to use a newer (>=9) JDK than the recommended version 8, you may experience build failures in the docker-maven-plugin. You can work around the failure by adding the following block after the plugin executions in the pom.xml
```xml
        <dependencies>
            <dependency>
                <groupId>javax.activation</groupId>
                <artifactId>activation</artifactId>
                <version>1.1.1</version>
            </dependency>
        </dependencies>
```

Windows users may experience a test failure in the UserControllerTest test during the build. You can work around that by changing the check on line 80 of that file from MediaType.APPLICATION_JSON_UTF_8 to MediaType.APPLICATION_JSON. Additionally, depending on your GIT configuration (core.autocrlf=true) you might need to fix the line endings of the shell scripts under src/main/docker to force unix line endings (e.g. `find src/main/docker -name "*.sh" -exec dos2unix {} \;`) in order for the Dockerfile to startup correctly. Docker is purely optional for these instructions, but FYI.
    
On initial startup (after running `java -jar athena.jar`), Athena will create the necessary schema objects in your Postgresql database. It is important to complete the step before moving on to the vocabulary import. 

By default, the application will use the application.properties file from ~/git/Athena/properties/dev. If you want to customize anything (for instance you have a remote instance of Postgresql), you can edit that file and rebuild or you can select a different environment by using one of the qa, test, or prod profiles.
    
## Import the vocabulary

1. Using a web browser, go to http://athena.ohdsi.org and click the Download link in the top right.
1. Sign up for an account if you don't already have one and then login.
1. Pick the terminologies that you want to import and submit the form.
1. Wait for an email with an appropriate download link for the terminology CSV data.

Once the terminology data is downloaded, you will want to edit the import_table function (see change below) and most likely rename the files from the provided download. Afterwards, run the safe_import_of_tables() script as shown below.
    
1. CHANGE the import_table function (lines 33-34)

    'COPY ' || quote_ident(table_name) || ' FROM ''/path/to/OMOP/vocabulary/' || csv_prefix || quote_ident(table_name) ||
      '.csv''' || ' delimiter E''\t'' escape E''"'' quote \E''\b'' csv HEADER encoding ''utf-8'';';

2. RENAME files

    ``ls *.csv | while read LINE ; do NEW=`echo $LINE | tr A-Z a-z | sed -e 's/\(.*\)/v5_\1/g'` ; mv $LINE $NEW ; done``

3. EXECUTE import

    `psql -U ohdsi -d athena_cdm_v5 -c 'select safe_import_of_tables();'`

## Populate SOLR index
Start the SOLR indexing operation by invoking the /dataimport RequestHandler with the full-import command.
```sh
curl http://localhost:8984/solr/concepts/dataimport?command=full-import
```

The full-import will take some time. You can use the status command to monitor the progress
```sh
curl http://localhost:8984/solr/concepts/dataimport?command=status
```

## Access Athena on local system

At this point, Athena should be operational. Use your web browser to navigate to http://localhost:3010/ to test it out.
