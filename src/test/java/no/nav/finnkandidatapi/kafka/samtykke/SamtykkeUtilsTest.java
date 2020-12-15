package no.nav.finnkandidatapi.kafka.samtykke;

import no.nav.finnkandidatapi.samtykke.Samtykke;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SamtykkeUtilsTest {
    private SamtykkeUtils samtykkeUtils = new SamtykkeUtils();

    @Test
    public void deserialiserMeldingOK() {
        String aktoerId = "1000068432771";
        String meldingType = "SAMTYKKE_OPPRETTET";
        String ressurs = "CV_HJEMMEL";
        String opprettetTidspunkt = "2019-04-01T13:17:13.174+02:00";

        String jsonMelding = lagJsonMelding(aktoerId, meldingType, ressurs, opprettetTidspunkt);
        Samtykke samtykke = samtykkeUtils.deserialiserMelding(jsonMelding);

        assertEquals(aktoerId, samtykke.getAktoerId());
        assertEquals(meldingType, samtykke.getEndring());
        assertEquals(ressurs, samtykke.getGjelder());

        ZonedDateTime tidspunktFraMappetSamtykke = ZonedDateTime.parse(opprettetTidspunkt);
        assertEquals(opprettetTidspunkt, tidspunktFraMappetSamtykke.toString());
    }

    @Test
    public void deserialiserMeldingMedManglendeFelt() {
        String jsonMeldingAktoerIdFeltMangler = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            samtykkeUtils.deserialiserMelding(jsonMeldingAktoerIdFeltMangler);
        });

        String jsonMeldingAktoerIdInneholderTomStreng = "{\"aktoerId\":\" \",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            samtykkeUtils.deserialiserMelding(jsonMeldingAktoerIdInneholderTomStreng);
        });

        String jsonMeldingManglerFeltViIkkeTrenger = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\"}";
        samtykkeUtils.deserialiserMelding(jsonMeldingManglerFeltViIkkeTrenger);
    }

    @Test
    public void deserialiserMeldingMedDatoFormatMedMillisekunder() {
        String jsonMelding = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        samtykkeUtils.deserialiserMelding(jsonMelding);
    }

    @Test
    public void deserialiserMeldingMedDatoFormat6SifreForMillisekunder() {
        String jsonMelding = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":\"2019-08-13T14:13:10.203505+02:00\",\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        samtykkeUtils.deserialiserMelding(jsonMelding);
    }

    private String lagJsonMelding(String aktoerId, String meldingType, String ressurs, String opprettetDato) {
        return "{\"aktoerId\":\"AktorId(aktorId=" + aktoerId + ")\",\"fnr\":\"27075349594\",\"meldingType\":\"" + meldingType + "\",\"ressurs\":\"" + ressurs + "\",\"opprettetDato\":\"" + opprettetDato + "\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
    }
}