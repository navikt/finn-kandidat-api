package no.nav.tag.finnkandidatapi;

import net.minidev.json.JSONObject;
import no.nav.tag.finnkandidatapi.kafka.HarTilretteleggingsbehov;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.Kandidatendring;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.logging.LoggEvent;
import no.nav.tag.finnkandidatapi.tilbakemelding.Behov;
import no.nav.tag.finnkandidatapi.tilbakemelding.Tilbakemelding;
import no.nav.tag.finnkandidatapi.sts.STSToken;
import no.nav.tag.finnkandidatapi.veilarbarena.Personinfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static no.nav.tag.finnkandidatapi.kandidat.ArbeidsmiljøBehov.*;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidstidBehov.HELTID;
import static no.nav.tag.finnkandidatapi.kandidat.ArbeidstidBehov.KAN_IKKE_JOBBE;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ARBEIDSSTILLING;
import static no.nav.tag.finnkandidatapi.kandidat.FysiskBehov.ERGONOMI;
import static no.nav.tag.finnkandidatapi.kandidat.GrunnleggendeBehov.*;

public class TestData {

    public static Kandidat enKandidat() {
        return Kandidat.builder()
                .sistEndret(LocalDateTime.now())
                .sistEndretAv(enNavIdent())
                .fnr("12345678901")
                .aktørId("1000000000001")
                .arbeidstidBehov(KAN_IKKE_JOBBE)
                .fysiskeBehov(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljøBehov(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK))
                .navKontor(etNavKontor())
                .build();
    }

    public static Kandidatendring enKandidatendring() {
        return Kandidatendring.builder()
                .aktørId("1000000000001")
                .arbeidstidBehov(HELTID)
                .fysiskeBehov(Set.of(ARBEIDSSTILLING))
                .arbeidsmiljøBehov(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER, ANNET))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, LESE_NORSK))
                .build();
    }

    public static String etNavKontor() {
        return "0325";
    }

    public static Personinfo personinfo() {
        return Personinfo.builder().navKontor(etNavKontor()).build();
    }

    public static Kandidat enKandidat(String aktørId) {
        return kandidatBuilder().aktørId(aktørId).build();
    }

    public static Kandidat.KandidatBuilder kandidatBuilder() {
        return Kandidat.builder()
                .sistEndret(LocalDateTime.now())
                .sistEndretAv(enNavIdent())
                .fnr("12345678901")
                .aktørId("1000000000001")
                .arbeidstidBehov(KAN_IKKE_JOBBE)
                .fysiskeBehov(Set.of(ARBEIDSSTILLING, ERGONOMI))
                .arbeidsmiljøBehov(Set.of(MENTOR, TILRETTELAGTE_ARBEIDSOPPGAVER))
                .grunnleggendeBehov(Set.of(SNAKKE_NORSK, SKRIVE_NORSK, LESE_NORSK));
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
}
