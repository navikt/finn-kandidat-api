package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SamtykkeMeldingValidatorTest {


    @Test
    public void deserialiserMeldingOK() {
        String aktoerId = "1000068432771";
        String meldingType = "SAMTYKKE_OPPRETTET";
        String ressurs = "CV_HJEMMEL";
        String opprettetTidspunkt = "2019-04-01T13:17:13.174+02:00";

        String jsonMelding = lagJsonMelding(aktoerId, meldingType, ressurs, opprettetTidspunkt);
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMelding);

        assertEquals(aktoerId, samtykkeMelding.getAktoerId());
        assertEquals(meldingType, samtykkeMelding.getMeldingType());
        assertEquals(ressurs, samtykkeMelding.getRessurs());
        assertEquals(ZonedDateTime.parse(opprettetTidspunkt).toLocalDateTime(), samtykkeMelding.getOpprettetDato());
    }

    @Test
    public void deserialiserMeldingMedManglendeFelt() {
        String jsonMeldingAktoerIdFeltMangler = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            SamtykkeMeldingValidator.valider(new SamtykkeMelding(jsonMeldingAktoerIdFeltMangler));
        });

        String jsonMeldingAktoerIdInneholderTomStreng = "{\"aktoerId\":\" \",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            SamtykkeMeldingValidator.valider(new SamtykkeMelding(jsonMeldingAktoerIdInneholderTomStreng));
        });

        String jsonMeldingManglerFeltViIkkeTrenger = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\"}";
        SamtykkeMeldingValidator.valider(new SamtykkeMelding(jsonMeldingManglerFeltViIkkeTrenger));
    }

    @Test
    public void deserialiserMeldingMedDatoFormatMedMillisekunder() {
        String jsonMelding = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        SamtykkeMeldingValidator.valider(new SamtykkeMelding(jsonMelding));
    }

    @Test
    public void deserialiserMeldingMedDatoFormat6SifreForMillisekunder() {
        String jsonMelding = "{\"aktoerId\":\"AktorId(aktorId=1000068432771)\",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":\"2019-08-13T14:13:10.203505+02:00\",\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        SamtykkeMeldingValidator.valider(new SamtykkeMelding(jsonMelding));
    }

    private String lagJsonMelding(String aktoerId, String meldingType, String ressurs, String opprettetDato) {
        return "{\"aktoerId\":\"AktorId(aktorId=" + aktoerId + ")\",\"fnr\":\"27075349594\",\"meldingType\":\"" + meldingType + "\",\"ressurs\":\"" + ressurs + "\",\"opprettetDato\":\"" + opprettetDato + "\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
    }
}