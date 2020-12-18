package no.nav.finnkandidatapi.samtykke;

import no.nav.finnkandidatapi.kafka.samtykke.SamtykkeMelding;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SamtykkeMeldingValidatorTest {

    private SamtykkeMeldingValidator samtykkeMeldingValidator = new SamtykkeMeldingValidator();

    @Test
    public void deserialiserMeldingOK() {
        String fnr = "1000068432771";
        String meldingType = "SAMTYKKE_OPPRETTET";
        String ressurs = "CV_HJEMMEL";
        String opprettetTidspunkt = "2019-04-01T13:17:13.174+02:00";

        String jsonMelding = lagJsonMelding(fnr, meldingType, ressurs, opprettetTidspunkt);
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMelding);
        samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
    }

    @Test
    public void deserialiserMeldingMedManglendeFelt() {
        String jsonMeldingAktoerIdFeltMangler = "{\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMeldingAktoerIdFeltMangler);
            samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
        });

        String jsonMeldingAktoerIdInneholderTomStreng = "{\"fnr\":\"\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
        assertThrows(RuntimeException.class, () -> {
            SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMeldingAktoerIdInneholderTomStreng);
            samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
        });

        String jsonMeldingManglerFeltViIkkeTrenger = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\"}";
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMeldingManglerFeltViIkkeTrenger);
        samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
    }

    @Test
    public void deserialiserMeldingMedDatoFormatMedMillisekunder() {
        String jsonMelding = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMelding);
        samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
    }

    @Test
    public void deserialiserMeldingMedDatoFormat6SifreForMillisekunder() {
        String jsonMelding = "{\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-04-01T13:17:13.174+02:00\",\"slettetDato\":\"2019-08-13T14:13:10.203505+02:00\",\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(jsonMelding);
        samtykkeMeldingValidator.validerSamtykkeMelding(samtykkeMelding);
    }

    private String lagJsonMelding(String fnr, String meldingType, String ressurs, String opprettetDato) {
        return "{\"fnr\":\"" + fnr + "\",\"meldingType\":\"" + meldingType + "\",\"ressurs\":\"" + ressurs + "\",\"opprettetDato\":\"" + opprettetDato + "\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":\"2019-04-08\"}";
    }
}