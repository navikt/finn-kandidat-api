package no.nav.finnkandidatapi;

import no.nav.finnkandidatapi.kafka.vedtakReplikert.Tokens;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakRad;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatDto;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.DinSituasjonSvarFraVeilarbReg;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.sts.STSToken;
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
                .tokens(Tokens.builder().fodselsnr("01010112345").build())
                .after(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .build())
                .build();
    }

    public static VedtakReplikert etDeleteVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("D")
                .tokens(Tokens.builder().fodselsnr("01010112345").build())
                .before(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .build())
                .build();
    }

    public static VedtakReplikert etAvslåttUpdateVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("U")
                .tokens(Tokens.builder().fodselsnr("01010112345").build())
                .after(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .utfallkode("NEI")
                        .build())
                .build();
    }

    public static VedtakReplikert etAvslåttDeleteVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("D")
                .tokens(Tokens.builder().fodselsnr("01010112345").build())
                .before(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .utfallkode("NEI")
                        .build())
                .build();
    }

    public static VedtakReplikert etAvslåttInsertVedtakReplikert() {
        return VedtakReplikert.builder()
                .op_type("I")
                .tokens(Tokens.builder().fodselsnr("01010112345").build())
                .after(VedtakRad.builder()
                        .vedtak_id(45L)
                        .rettighetkode("PERM")
                        .utfallkode("NEI")
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

    public static MidlertidigUtilgjengelig enMidlertidigUtilgjengelig(String aktørid) {
        return MidlertidigUtilgjengelig.builder()
                .aktørId(aktørid)
                .fraDato(now())
                .tilDato(now().plusDays(7))
                .registrertAvIdent("A100000")
                .registrertAvNavn("Ola Nordmann")
                .sistEndretAvIdent("B200000")
                .sistEndretAvNavn("Kari Nordmann")
                .build();
    }

    public static MidlertidigUtilgjengelig enMidlertidigUtilgjengeligMedBareNull() {
        return MidlertidigUtilgjengelig.builder()
                .aktørId("1000000011")
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

    private static final Clock milliesClock = Clock.tickMillis(ZoneId.systemDefault());


    /**
     * <p>
     * Metoden gir timestamps med millisekunds presisjon hvor nanosekunder er trunkert. Nødvendig fordi ulike
     * maskiner og operativsystemer har ulik støtte for nanoskeunders presisjon. Det har ført til at tester har vært
     * grønne på Mac og røde på Windows. Se
     * https://stackoverflow.com/questions/52029920/localdatetime-now-has-different-levels-of-precision-on-windows-and-mac-machine
     * </p><p>
     * Bruker metodenavnet now() for å gjøre det vanskeligere å bruke java.time.now() ved en feiltagelse.
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
