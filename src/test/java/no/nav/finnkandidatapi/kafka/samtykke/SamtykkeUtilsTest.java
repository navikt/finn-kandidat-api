package no.nav.finnkandidatapi.kafka.samtykke;

import no.nav.finnkandidatapi.samtykke.Samtykke;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SamtykkeUtilsTest {
    private SamtykkeUtils samtykkeUtils = new SamtykkeUtils();

    @Test
    public void deserialiserMeldingOK() {
        String aktoerId = "1000068432771";
        String meldingType = "SAMTYKKE_OPPRETTET";
        String ressurs = "CV_HJEMMEL";
        String jsonMelding = lagJsonMelding(aktoerId, meldingType, ressurs);

        Samtykke samtykke = samtykkeUtils.deserialiserMelding(jsonMelding);

        assertEquals(aktoerId, samtykke.getAktoerId());
        assertEquals(meldingType, samtykke.getEndring());
        assertEquals(ressurs, samtykke.getGjelder());
    }

    @Test
    public void deserialiserMeldingMedManglendeFelt() {
        String jsonMeldingAktoerIdFeltMangler = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        assertThrows(RuntimeException.class, () -> {
            samtykkeUtils.deserialiserMelding(jsonMeldingAktoerIdFeltMangler);
        });

        String jsonMeldingAktoerIdInneholderTomStreng = "{\"aktoerId\":\" \",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        assertThrows(RuntimeException.class, () -> {
            samtykkeUtils.deserialiserMelding(jsonMeldingAktoerIdInneholderTomStreng);
        });

        String jsonMeldingManglerFeltViIkkeTrenger = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\"}";
        samtykkeUtils.deserialiserMelding(jsonMeldingManglerFeltViIkkeTrenger);
    }

    private String lagJsonMelding(String aktoerId, String meldingType, String ressurs) {
        return "{\"aktoerId\":\"AktorId(aktorId=" + aktoerId + ")\",\"fnr\":\"27075349594\",\"meldingType\":\"" + meldingType + "\",\"ressurs\":\"" + ressurs + "\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
    }
}