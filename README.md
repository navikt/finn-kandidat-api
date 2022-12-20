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
Du kan gå inn i "Arbeidsrettet oppfølging" på https://veilarbpersonflate.dev.intern.nav.no/ og søke opp en arbeidssøker fra Rekrutteringsbistand med personnummer. 
Her kan du legge til eller slette tilretteleggingsbehov. Man kan verifisere endringene ved å bruke filtrene for tilretteleggingsbehov i Rekrutteringsbistands kandidatsøk.

Du kan verifisere at en kandidat sine tilretteleggingsbehov vises i en arbeidssøkers "Din side" på nav.no slik:
- Gå til https://www.dev.nav.no/person/personopplysninger/nb/
- Logg inn ved å velge "TestID" og skriv inn fødselsnummeret til en arbeidssøker fra Rekrutteringsbistand
- Scroll ned til og ekspander/klikk på "Behov for tilrettelegging".

## Database for utvikling

Vi bruker `h2` som database under utvikling når du kjører lokalt. For å se, endre og slette kandidater kan du logge deg inn på [localhost:8080/finn-kandidat-api/h2](http://localhost:8080/finn-kandidat-api/h2). Bruk standard brukernavn og passord og følgende URL:
- JDBC URL: jdbc:h2:mem:testdb
- Brukernavn: sa
- Passord: <ingenting\>
Ev. sette opp en 

# Henvendelser

## For Nav-ansatte

* Dette Git-repositoriet eies
  av [team Toi i produktområde Arbeidsgiver](https://teamkatalog.nav.no/team/76f378c5-eb35-42db-9f4d-0e8197be0131).
* Slack-kanaler:
    * [#arbeidsgiver-toi-dev](https://nav-it.slack.com/archives/C02HTU8DBSR)
    * [#rekrutteringsbistand-værsågod](https://nav-it.slack.com/archives/C02HWV01P54)

## For folk utenfor Nav

IT-avdelingen
i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)
