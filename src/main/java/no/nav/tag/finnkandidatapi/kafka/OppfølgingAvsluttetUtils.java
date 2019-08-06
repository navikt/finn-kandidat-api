package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class OppfølgingAvsluttetUtils {
    public static OppfølgingAvsluttetMelding deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = mapper.readValue(melding, OppfølgingAvsluttetMelding.class);

            if (oppfølgingAvsluttetMelding.getAktorId() == null || oppfølgingAvsluttetMelding.getSluttdato() == null) {
                throw new RuntimeException("Kunne ikke deserialisere OppfølgingAvsluttetMelding, melding: " + oppfølgingAvsluttetMelding);
            }

            return oppfølgingAvsluttetMelding;

        } catch (IOException e) {
            // TODO:
            //  Ha overvåkning på dette i Kibana board
            //  Skriv test for om dette feiler
            //  Sjekk om default er at melding blir konsumert igjen hvis noe feiler
//            log.error("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
            throw new RuntimeException("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
        }
    }
}
