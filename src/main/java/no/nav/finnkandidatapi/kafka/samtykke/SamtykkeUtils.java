package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.samtykke.Samtykke;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SamtykkeUtils {

    public static Samtykke deserialiserMelding(String jsonMelding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SamtykkeMelding samtykkeMelding = mapper.readValue(jsonMelding, SamtykkeMelding.class);
            validerSamtykkeMelding(samtykkeMelding);
            log.info("Samtykkemelding:"
                    + " Opprettet dato:" + samtykkeMelding.getOpprettetDato()
                    + " Slettet dato: " + samtykkeMelding.getSlettetDato()
                    + " Meldingstype:" + samtykkeMelding.getMeldingType()
                    + " Ressurs:" + samtykkeMelding.getRessurs());

            return mapFraSamtykkeMelding(samtykkeMelding);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere samtykkemelding", e);
        }
    }

    private static void validerSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        if (samtykkeMelding.getAktoerId() == null) {
            throw new RuntimeException("AktørID er null");
        }

        if (samtykkeMelding.getMeldingType() == null) {
            throw new RuntimeException("Meldingtype er null");
        }

        if (samtykkeMelding.getRessurs() == null) {
            throw new RuntimeException("Ressurs er null");
        }

//        if (samtykkeMelding.getOpprettetDato() == null) {
//            throw new RuntimeException("OpprettetDato er null");
//        }
    }

    private static Samtykke mapFraSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        String aktoerId = hentAlleTallFraString(samtykkeMelding.getAktoerId());

        int korrektLengdeAktoerId = 13;
        if (aktoerId.length() != korrektLengdeAktoerId) {
            throw new RuntimeException("AktørID må ha 13 tegn :" + samtykkeMelding.getAktoerId());
        }
        return new Samtykke(aktoerId, samtykkeMelding.getRessurs(), samtykkeMelding.getMeldingType(), samtykkeMelding.getOpprettetDato());
    }

    private static String hentAlleTallFraString(String stringMedTall) {
        return stringMedTall.replaceAll("\\D+", "");
    }
}
