# Backend til finn-kandidat 

En backend for Finn kandidat, skrevet i Spring.


## Kjøre applikasjonen lokalt 

IntelliJ: Kjør klassen FinnKandidatApiApplication. 

Kommandolinje: Kjør `mvn spring:boot run` (OBS: Dette funker ikke for meg per april 2020. Are Husby)


## API-dokumentasjon
Swagger-dokumentajonen finnes på http://localhost:8080/finn-kandidat-api/swagger-ui.html

### Autentisering når appen kjører lokalt
Bruk Swagger UI til å føst sende en GET til et av endepunktene i LokalLoginController.
- For å logge inn som veileder: GET `/local/veileder-cookie`
- For å logge inn som ektern bruker: GET `/local/ekstern-bruker-cookie`

Deretter kall det endepunktet du ønsker å teste.
 

## Manuell test i dev
Du kan gå inn i "Arbeidsrettet oppfølging" på https://app-q0.adeo.no/veilarbpersonflatefs/10057529976?enhet=0104 og f.eks. registrere tilretteleggingsbehov.
Brukernavn: Z994379

Hvis du vil registrere på en annen person kan du gå via dekoratøren > Enhetens oversikt > Søke opp en person > Gå inn på personen > Detaljer

Du kan verifisere at en kandidat sine tilretteleggingsbehov vises i den personen sin "Din side" på nav.no slik:
- Hvis di ikke allerede har gjort det, legg til stub-oidc-provider her: http://myapps.microsoft.com/ og vent på at requesten blir godkjent
- Gå til https://www-q0.nav.no/person/personopplysninger
- Logg først inn med din egen NAV-ident. Deretter logg inn "Uten IDPorten", fnr: 10108000398, eller med et fnr du har hentet fra Rekrutteringsbistand.
- Scroll ned til og ekspander/klikk på "Behov for tilrettelegging".

## Database for utvikling

Vi bruker `h2` som database under utvikling når du kjører lokalt. For å se, endre og slette kandidater kan du logge deg inn på [localhost:8080/finn-kandidat-api/h2](http://localhost:8080/finn-kandidat-api/h2). Bruk standard brukernavn og passord og følgende URL:
- JDBC URL: jdbc:h2:mem:testdb
- Brukernavn: sa
- Passord: <ingenting\>
Ev. sette opp en 

# Henvendelser

## For Nav-ansatte
* Dette Git-repositoriet eies av [Team inkludering i Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
  * [#inkludering-utvikling](https://nav-it.slack.com/archives/CQZU35J6A)
  * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)
  * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål.
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)
