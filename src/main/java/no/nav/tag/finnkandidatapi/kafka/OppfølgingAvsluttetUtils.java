package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class OppfølgingAvsluttetUtils {
    public static OppfølgingAvsluttetMelding deserialiserMelding(String melding) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(melding, OppfølgingAvsluttetMelding.class);
    }
}
