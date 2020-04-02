package no.nav.finnkandidatapi;

import net.minidev.json.JSONObject;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatDto;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.logging.LoggEvent;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.DinSituasjonSvarFraVeilarbReg;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.sts.STSToken;
import no.nav.finnkandidatapi.tilbakemelding.Behov;
import no.nav.finnkandidatapi.tilbakemelding.Tilbakemelding;
import no.nav.finnkandidatapi.veilarbarena.Oppfølgingsbruker;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static no.nav.finnkandidatapi.kandidat.Arbeidshverdagen.*;
import static no.nav.finnkandidatapi.kandidat.Arbeidstid.GRADVIS_ØKNING;
import static no.nav.finnkandidatapi.kandidat.Arbeidstid.KAN_IKKE_JOBBE;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ARBEIDSSTILLING;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ERGONOMI;
import static no.nav.finnkandidatapi.kandidat.UtfordringerMedNorsk.*;

public class TestData {

    public static ArbeidssokerRegistrertDTO enKjentArbeidssokerRegistrering() {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId("1000000000001")
                .status(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .registreringTidspunkt(LocalDateTime.now())
                .build();
    }

    public static ArbeidssokerRegistrertDTO enUkjentArbeidssokerRegistrering() {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId("1000000000002")
                .status(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .registreringTidspunkt(LocalDateTime.now())
                .build();
    }

    public static PermittertArbeidssoker enPermittertArbeidssoker() {
        return PermittertArbeidssoker.builder()
                .aktørId("1000000000001")
                .statusFraVeilarbRegistrering(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .tidspunktForStatusFraVeilarbRegistrering(LocalDateTime.now())
                .build();
    }

    public static PermittertArbeidssoker enLagretPermittertArbeidssoker(Integer id) {
        return PermittertArbeidssoker.builder()
                .id(id)
                .aktørId("1000000000001")
                .statusFraVeilarbRegistrering(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .tidspunktForStatusFraVeilarbRegistrering(LocalDateTime.now())
                .build();
    }

    public static PermittertArbeidssoker enTomPermittertArbeidssoker() {
        return PermittertArbeidssoker.builder()
                .aktørId("1000000000001")
                .build();
    }

    public static Kandidat enKandidat() {
        return Kandidat.builder()
                .sistEndretAvVeileder(now())
                .sistEndretAv(enNavIdent())
                .fnr(etFnr())
                .aktørId("1000000000001")
                .arbeidstid(Set.of(KAN_IKKE_JOBBE))
                .fysisk(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidshverdagen(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .utfordringerMedNorsk(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .navKontor(etNavKontor())
                .build();
    }

    public static KandidatDto enKandidatDto(Kandidat kandidat) {
        return KandidatDto.builder()
                .fnr(kandidat.getFnr())
                .aktørId(kandidat.getAktørId())
                .arbeidstid(kandidat.getArbeidstid())
                .fysisk(kandidat.getFysisk())
                .arbeidshverdagen(kandidat.getArbeidshverdagen())
                .utfordringerMedNorsk(kandidat.getUtfordringerMedNorsk())
                .build();
    }

    public static KandidatDto enKandidatDto() {
        return KandidatDto.builder()
                .fnr(etFnr())
                .aktørId("1000000000001")
                .arbeidstid(Set.of(GRADVIS_ØKNING))
                .fysisk(Set.of(ARBEIDSSTILLING))
                .arbeidshverdagen(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER, ANNET))
                .utfordringerMedNorsk(Set.of(SNAKKE_NORSK, LESE_NORSK))
                .build();
    }

    public static String etNavKontor() {
        return "0325";
    }

    public static Oppfølgingsbruker enOppfølgingsbruker() {
        return new Oppfølgingsbruker(etFnr(), etNavKontor());
    }

    public static Kandidat enKandidat(String aktørId) {
        return kandidatBuilder().aktørId(aktørId).build();
    }

    public static Kandidat.KandidatBuilder kandidatBuilder() {
        return Kandidat.builder()
                .sistEndretAvVeileder(now())
                .sistEndretAv(enNavIdent())
                .fnr("12345678901")
                .aktørId("1000000000001")
                .arbeidstid(Set.of(KAN_IKKE_JOBBE))
                .fysisk(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidshverdagen(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .utfordringerMedNorsk(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK));
    }

    public static Veileder enVeileder() {
        return new Veileder("X123456");
    }

    public static String enNavIdent() {
        return "Y123456";
    }

    public static String enAktørId() {
        return "123";
    }

    public static String etFnr() {
        return "28037639429";
    }

    public static Kandidat enKandidatMedNullOgTommeSet() {
        return Kandidat.builder()
                .arbeidstid(Collections.emptySet())
                .fysisk(Collections.emptySet())
                .arbeidshverdagen(Collections.emptySet())
                .utfordringerMedNorsk(Collections.emptySet())
                .build();
    }

    public static Kandidat enKandidatMedBareNull() {
        return Kandidat.builder().build();
    }

    public static STSToken etStsToken() {
        return new STSToken("-", "-", 100);
    }

    public static Tilbakemelding enTilbakemelding() {
        return new Tilbakemelding(
                Behov.ARBEIDSTID,
                "kul tilbakemelding"
        );
    }

    public static LoggEvent enLoggEvent() {
        String name = "eventnavn";
        JSONObject tags = new JSONObject(Map.of("tag1", "verdi1", "tag2", "verdi2"));
        JSONObject fields = new JSONObject(Map.of("field1", "verdi1", "field2", 2));
        return new LoggEvent(name, tags, fields);
    }

    public static LoggEvent enLoggEventMedIntTag() {
        String name = "eventnavn";
        JSONObject tags = new JSONObject(Map.of("tag1", 1, "tag2", "verdi2"));
        JSONObject fields = new JSONObject(Map.of("field1", "verdi1", "field2", 2));
        return new LoggEvent(name, tags, fields);
    }

    private static final Clock milliesClock = Clock.tickMillis(ZoneId.systemDefault());


    /**
     * <p>
     * Metoden gir timestamps med millisekunds presisjon hvor nanosekunder er trunkert. Nødvendig fordi ulike
     * maskiner og operativsystemer har ulik støtte for nanoskeunders presisjon. Det har ført til at tester har vært
     * grønne på Mac og røde på Windows. Se
     * https://stackoverflow.com/questions/52029920/localdatetime-now-has-different-levels-of-precision-on-windows-and-mac-machine
     * </p><p>
     * Bruker metodenavnet now() for å gjøre det vanskeligere å bruke java.time.LocalDateTime.now() ved en feiltagelse.
     * </p><p>
     * Returnert timestamp vil være garantert unik også ved flere kall innenfor samme millisekund, fordi metoden pauser tråden i ett millisekund.
     * </p>
     *
     * @return Et unikt tidspunkt nært nå med millisekunders presisisjon, uten nanosekunder.
     */
    public static LocalDateTime now() {
        try {
            Thread.sleep(1L); // Sikre unike millisekund timestamps
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return LocalDateTime.now(milliesClock);
    }
}
