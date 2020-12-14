package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.finnkandidatapi.samtykke.Samtykke;

import java.io.IOException;

public class SamtykkeUtils {

    public Samtykke deserialiserMelding(String jsonMelding) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            SamtykkeMelding samtykkeMelding = mapper.readValue(jsonMelding, SamtykkeMelding.class);
            validerSamtykkeMelding(samtykkeMelding);

            return mapFraSamtykkeMelding(samtykkeMelding);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere samtykkemelding", e);
        }
    }

    private void validerSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        if (samtykkeMelding.getAktoerId() == null) {
            throw new RuntimeException("AktørID er null: " + samtykkeMelding.getAktoerId());
        }

        if (samtykkeMelding.getMeldingType() == null) {
            throw new RuntimeException("Meldingtype er null: " + samtykkeMelding.getMeldingType());
        }

        if (samtykkeMelding.getRessurs() == null) {
            throw new RuntimeException("Ressurs er null: " + samtykkeMelding.getRessurs());
        }
    }

    private Samtykke mapFraSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        String aktoerId = hentAlleTallFraString(samtykkeMelding.getAktoerId());

        int korrektLengdeAktoerId = 13;
        if (aktoerId.length() != korrektLengdeAktoerId) {
            throw new RuntimeException("AktørID må ha 13 tegn :" + samtykkeMelding.getAktoerId());
        }
        return new Samtykke(aktoerId, samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType());
    }

    private String hentAlleTallFraString(String stringMedTall) {
        return stringMedTall.replaceAll("\\D+", "");
    }
}
