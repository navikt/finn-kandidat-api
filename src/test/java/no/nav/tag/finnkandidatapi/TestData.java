package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSToken;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.FADDER;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.TILRETTELAGTE_ARBEIDSOPPGAVER;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidstidBehov.KAN_IKKE_JOBBE;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ERGONOMI;
import static no.nav.tag.finnkandidatapi.kandidat.GrunnleggendeBehov.*;

public class TestData {

    public static STSToken etStsToken() {
        return new STSToken("asdfasdgdf", "type", 5000);
    }

    public static Kandidat enKandidat() {
        return Kandidat.builder()
                .sistEndret(LocalDateTime.now())
                .sistEndretAv(enNavIdent())
                .fnr("12345678901")
                .arbeidstidBehov(KAN_IKKE_JOBBE)
                .fysiskeBehov(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljøBehov(Set.of(FADDER, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .build();
    }

    public static Kandidat enKandidat(String fnr) {
        return kandidatBuilder().fnr(fnr).build();
    }

    public static Kandidat.KandidatBuilder kandidatBuilder() {
        return Kandidat.builder()
                .sistEndret(LocalDateTime.now())
                .sistEndretAv(enNavIdent())
                .fnr("12345678901")
                .arbeidstidBehov(KAN_IKKE_JOBBE)
                .fysiskeBehov(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljøBehov(Set.of(FADDER, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK));
    }

    public static Veileder enVeileder() {
        return new Veileder("X123456");
    }

    public static String enNavIdent() {
        return "Y123456";
    }

    public static Kandidat enKandidatMedNullOgTommeSet() {
        return Kandidat.builder()
                .fysiskeBehov(Collections.emptySet())
                .arbeidsmiljøBehov(Collections.emptySet())
                .grunnleggendeBehov(Collections.emptySet())
                .build();
    }

    public static Kandidat enKandidatMedBareNull() {
        return Kandidat.builder().build();
    }
}
