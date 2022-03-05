#! /bin/bash
java -Djava.security.egd=file:/dev/./urandom -jar athena.jar --spring.config.additional-location=application.properties
exit 0