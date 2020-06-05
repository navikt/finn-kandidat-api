FROM navikt/java:13
COPY init.sh /init-scripts/init.sh
COPY /target/finn-kandidat-api-0.0.1-SNAPSHOT.jar app.jar