# Backend til finn-kandidat

En backend for Finn kandidat, skrevet i Spring.

## Installasjon

Bruk en IDE eller kjør:

```
mvn install
mvn spring:boot run
```

## Database for utvikling

Vi bruker `h2` som database under utvikling. For å se, endre og slette kandidater kan du logge deg inn på [localhost:8080/finn-kandidat-api/h2](http://localhost:8080/finn-kandidat-api/h2). Bruk standard brukernavn og passord og følgende URL:

```
JDBC URL: jdbc:h2:mem:testdb
```
