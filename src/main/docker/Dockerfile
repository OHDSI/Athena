FROM openjdk:8u282-jre-slim
LABEL maintainer="Odysseus Inc."

USER root
VOLUME /tmp
COPY athena.jar /athena.jar
COPY run.sh /bin/run.sh
COPY application.properties /application.properties

COPY cpt4_5 /opt/athena/addons/cpt4_5
COPY cpt4_4_5 /opt/athena/addons/cpt4_4_5

COPY cpt4.jar /opt/athena/addons/cpt4_5
COPY cpt4.jar /opt/athena/addons/cpt4_4_5

RUN chmod +x /bin/run.sh
EXPOSE 8081
CMD ["/bin/run.sh"]