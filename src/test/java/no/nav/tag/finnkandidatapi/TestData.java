package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Arbeidstid;
import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Tilretteleggingsbehov;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Fysisk.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Fysisk.ERGONOMI;

public class TestData {

    public static Tilretteleggingsbehov etTilretteleggingsbehov() {
        return Tilretteleggingsbehov.builder()
                .opprettet(LocalDateTime.now())
                .opprettetAvIdent("X123456")
                .brukerFnr("12345678901")
                .arbeidstid(Arbeidstid.KAN_IKKE_JOBBE)
                .fysisk(new ArrayList<>(Arrays.asList(ARBEIDSSTILLING, ERGONOMI)))
                .build();
    }
}
