FROM navikt/java:11
COPY /target/finn-kandidat-api-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local"