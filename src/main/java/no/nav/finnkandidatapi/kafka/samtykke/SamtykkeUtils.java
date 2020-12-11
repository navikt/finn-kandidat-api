package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.finnkandidatapi.samtykke.Samtykke;

import java.io.IOException;

public class SamtykkeUtils {

    public Samtykke deserialiserMelding(String jsonMelding) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            SamtykkeMelding samtykkeMelding = mapper.readValue(jsonMelding, SamtykkeMelding.class);
            Samtykke samtykke = mapFraSamtykkeMelding(samtykkeMelding);

            if (!opprettetSamtykkeErGyldig(samtykke)) {
                throw new RuntimeException("Samtykkemelding er ugyldig: " + jsonMelding);
            }
            return samtykke;
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere samtykkemelding", e);
        }
    }

    private Samtykke mapFraSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        return new Samtykke(samtykkeMelding.getAktoerId(), samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType());
    }

    private boolean opprettetSamtykkeErGyldig(Samtykke samtykke) {
        return gyldigFeltVerdi(samtykke.getAktoerId()) && gyldigFeltVerdi(samtykke.getEndring()) && gyldigFeltVerdi(samtykke.getGjelder());
    }

    private boolean gyldigFeltVerdi(String feltVerdi) {
        return feltVerdi != null && !feltVerdi.isBlank();
    }
}
