package no.nav.finnkandidatapi;

import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakRad;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatDto;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.DinSituasjonSvarFraVeilarbReg;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.tilbakemelding.Behov;
import no.nav.finnkandidatapi.tilbakemelding.Tilbakemelding;
import no.nav.finnkandidatapi.vedtak.Vedtak;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;

import static no.nav.finnkandidatapi.kandidat.Arbeidshverdagen.*;
import static no.nav.finnkandidatapi.kandidat.Arbeidstid.GRADVIS_ØKNING;
import static no.nav.finnkandidatapi.kandidat.Arbeidstid.KAN_IKKE_JOBBE;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ARBEIDSSTILLING;
import static no.nav.finnkandidatapi.kandidat.Fysisk.ERGONOMI;
import static no.nav.finnkandidatapi.kandidat.UtfordringerMedNorsk.*;

public class TestData {

    public static VedtakReplikert etUpdateVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("U")
                .fodselsnr("01010112345")
                .after(VedtakRad.builder()
                        .utfallkode("JA")
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .build())
                .build();
    }

    public static VedtakReplikert etDeleteVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("D")
                .fodselsnr("01010112345")
                .before(VedtakRad.builder()
                        .utfallkode("JA")
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .build())
                .build();
    }

    public static VedtakReplikert etVedtakAfterReplikert(String opType, String utfallKode) {
        return VedtakReplikert.builder()
                .op_type(opType)
                .fodselsnr("01010112345")
                .after(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .utfallkode(utfallKode)
                        .build())
                .build();
    }

    public static VedtakReplikert etVedtakBeforeReplikert(String opType, String utfallKode) {
        return VedtakReplikert.builder()
                .op_type(opType)
                .fodselsnr("01010112345")
                .before(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .utfallkode(utfallKode)
                        .build())
                .build();
    }

    public static Vedtak etVedtak() {
        return Vedtak.builder()
                .aktørId("1000000000001")
                .arenaDbOperasjon("U")
                .arenaDbTidsstempel(now())
                .arenaDbTransactionlogPosisjon("1")
                .fnr("01010112345")
                .fraDato(now().minusMonths(5))
                .tilDato(now().minusMonths(1))
                .personId(1000L)
                .rettighetKode("PERM")
                .slettet(false)
                .statusKode("IVERK")
                .typeKode("E")
                .utfallKode("JA")
                .vedtakId(101L)
                .build();
    }

    public static Vedtak etAvsluttetVedtak() {
        return Vedtak.builder()
                .aktørId("1000000000001")
                .arenaDbOperasjon("U")
                .arenaDbTidsstempel(now())
                .arenaDbTransactionlogPosisjon("1")
                .fnr("01010112345")
                .fraDato(now().minusMonths(5))
                .tilDato(now().minusMonths(1))
                .personId(1000L)
                .rettighetKode("PERM")
                .slettet(false)
                .statusKode("AVSLU")
                .typeKode("E")
                .utfallKode("JA")
                .vedtakId(101L)
                .build();
    }

    public static Vedtak etTomtVedtak() {
        return Vedtak.builder().build();
    }

    public static ArbeidssokerRegistrertDTO enKjentArbeidssokerRegistrering() {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId("1000000000001")
                .status(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .registreringTidspunkt(now())
                .build();
    }

    public static ArbeidssokerRegistrertDTO enUkjentArbeidssokerRegistrering() {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId("1000000000002")
                .status(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .registreringTidspunkt(now())
                .build();
    }

    public static PermittertArbeidssoker enPermittertArbeidssoker() {
        return PermittertArbeidssoker.builder()
                .aktørId("1000000000001")
                .statusFraVeilarbRegistrering(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .tidspunktForStatusFraVeilarbRegistrering(now())
                .build();
    }

    public static PermittertArbeidssoker enLagretPermittertArbeidssoker(Integer id) {
        return PermittertArbeidssoker.builder()
                .id(id)
                .aktørId("1000000000001")
                .statusFraVeilarbRegistrering(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name())
                .tidspunktForStatusFraVeilarbRegistrering(now())
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
                .build();
    }

    public static Kandidat enKandidatMedSyntetiskFødselsnummer() {
        return Kandidat.builder()
                .sistEndretAvVeileder(now())
                .sistEndretAv(enNavIdent())
                .fnr(etSyntetiskFødselsnummer())
                .aktørId("1000000000002")
                .arbeidstid(Set.of(KAN_IKKE_JOBBE))
                .fysisk(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidshverdagen(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .utfordringerMedNorsk(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
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
        return new Veileder("X123456", etFornavn() + " " + etEtternavn());
    }

    public static String etFornavn() {
        return "Ola";
    }

    public static String etEtternavn() {
        return "Nordmann";
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

    public static String etSyntetiskFødselsnummer() {
        return "25879099636";
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

    public static Tilbakemelding enTilbakemelding() {
        return new Tilbakemelding(
                Behov.ARBEIDSTID,
                "kul tilbakemelding"
        );
    }

    private static final Clock milliesClock = Clock.tickMillis(ZoneId.systemDefault());


    /**
     * <p>
     * Metoden gir timestamps med millisekunds presisjon hvor nanosekunder er trunkert. Nødvendig fordi ulike
     * maskiner og operativsystemer har ulik støtte for nanoskeunders presisjon. Det har ført til at tester har vært
     * grønne på Mac og røde på Windows og Linux. Se
     * https://stackoverflow.com/questions/52029920/localdatetime-now-has-different-levels-of-precision-on-windows-and-mac-machine
     * </p><p>
     * Bruker metodenavnet now() for å gjøre det vanskeligere å bruke java.time.now() ved en feiltagelse.
     * </p>
     *
     * @return Et unikt tidspunkt nært nå med millisekunders presisisjon, uten nanosekunder.
     */
    public static synchronized LocalDateTime now() {
        return LocalDateTime.now(milliesClock);
    }
}
