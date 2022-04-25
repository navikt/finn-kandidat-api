# Backend til finn-kandidat 

En backend for Finn kandidat, skrevet i Spring.


## Kjøre applikasjonen lokalt 

IntelliJ: Kjør klassen FinnKandidatApiApplication. 

Kommandolinje: Kjør `mvn spring:boot run` (OBS: Dette funker ikke for meg per april 2020. Are Husby)

### Autentisering når appen kjører lokalt
Bruk Insomnia eller Postman til å først sende en GET til et av endepunktene i LokalLoginController.
- For å logge inn som veileder: GET `/local/veileder-cookie`
- For å logge inn som ektern bruker: GET `/local/ekstern-bruker-cookie`

Deretter kall det endepunktet du ønsker å teste.
 

## Manuell test i dev
Du kan gå inn i "Arbeidsrettet oppfølging" på https://app-q1.adeo.no/veilarbpersonflatefs og søke opp en arbeidssøker fra Rekrutteringsbistand med personnummer. 
Her kan du legge til eller slette tilretteleggingsbehov. Man kan verifisere endringene ved å bruke filtrene for tilretteleggingsbehov i Rekrutteringsbistands kandidatsøk.

Du kan verifisere at en kandidat sine tilretteleggingsbehov vises i en arbeidssøkers "Din side" på nav.no slik:
- Gå til https://www.dev.nav.no/person/personopplysninger/nb/
- Logg inn "Uten IDPorten" og skriv inn fødselsnummeret til en arbeidssøker fra Rekrutteringsbistand
- Scroll ned til og ekspander/klikk på "Behov for tilrettelegging".

Per i dag har vi dessverre ingen testbrukere i Rekrutteringsbistand som kan logges inn som privatpersoner i dev.nav.no. Dette skyldes at 
kun personnumre som er registrert i Skatteetatens testmiljø kan benyttes til innlogging med BankID. 

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
