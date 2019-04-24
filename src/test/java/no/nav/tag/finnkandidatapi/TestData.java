package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Tilretteleggingsbehov;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Arbeidsmiljo.FADDER;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Arbeidsmiljo.TILRETTELAGT_ARBEIDSOPPGAVER;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Arbeidstid.KAN_IKKE_JOBBE;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Fysisk.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Fysisk.ERGONOMI;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Grunnleggende.*;

public class TestData {

    public static Tilretteleggingsbehov etTilretteleggingsbehov() {
        return Tilretteleggingsbehov.builder()
                .opprettet(LocalDateTime.now())
                .opprettetAvIdent("X123456")
                .brukerFnr("12345678901")
                .arbeidstid(KAN_IKKE_JOBBE)
                .fysisk(Arrays.asList(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljo(Arrays.asList(FADDER, TILRETTELAGT_ARBEIDSOPPGAVER))
                .grunnleggende(Arrays.asList(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .build();
    }

    public static Tilretteleggingsbehov etTilretteleggingsbehovMedNullOgTommeLister() {
        return Tilretteleggingsbehov.builder()
                .fysisk(Collections.emptyList())
                .arbeidsmiljo(Collections.emptyList())
                .grunnleggende(Collections.emptyList())
                .build();
    }

    public static Tilretteleggingsbehov etTilretteleggingsbehovMedNull() {
        return Tilretteleggingsbehov.builder().build();
    }
}
