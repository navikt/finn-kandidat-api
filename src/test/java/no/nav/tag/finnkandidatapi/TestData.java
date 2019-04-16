package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Arbeidstid;
import no.nav.tag.finnkandidatapi.tilretteleggingsbehov.Tilretteleggingsbehov;

import java.time.LocalDateTime;

public class TestData {

    public static Tilretteleggingsbehov etTilretteleggingsbehov() {
        return Tilretteleggingsbehov.builder()
                .opprettet(LocalDateTime.now())
                .opprettetAvIdent("X123456")
                .brukerFnr("12345678901")
                .arbeidstid(Arbeidstid.KAN_IKKE_JOBBE)
                .build();
    }
}
