package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.kandidat.Kandidat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

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
                .fysiskeBehov(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljoBehov(Set.of(FADDER, TILRETTELAGT_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .build();
    }

    public static Kandidat enKandidatMedNullOgTommeSet() {
        return Kandidat.builder()
                .fysiskeBehov(Collections.emptySet())
                .arbeidsmiljoBehov(Collections.emptySet())
                .grunnleggendeBehov(Collections.emptySet())
                .build();
    }

    public static Kandidat enKandidatMedBareNull() {
        return Kandidat.builder().build();
    }
}
