package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.kandidat.Kandidat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljoBehov.FADDER;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljoBehov.TILRETTELAGT_ARBEIDSOPPGAVER;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidstidBehov.KAN_IKKE_JOBBE;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ERGONOMI;
import static no.nav.tag.finnkandidatapi.kandidat.GrunnleggendeBehov.*;

public class TestData {

    public static Kandidat enKandidat() {
        return Kandidat.builder()
                .sistEndret(LocalDateTime.now())
                .sistEndretAv("X123456")
                .fnr("12345678901")
                .arbeidstidBehov(KAN_IKKE_JOBBE)
                .fysiskeBehov(Arrays.asList(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljoBehov(Arrays.asList(FADDER, TILRETTELAGT_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Arrays.asList(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .build();
    }

    public static Kandidat enKandidatMedNullOgTommeLister() {
        return Kandidat.builder()
                .fysiskeBehov(Collections.emptyList())
                .arbeidsmiljoBehov(Collections.emptyList())
                .grunnleggendeBehov(Collections.emptyList())
                .build();
    }

    public static Kandidat enKandidatMedBareNull() {
        return Kandidat.builder().build();
    }
}
