package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

public class OppfølgingAvsluttetUtils {
    public static OppfølgingAvsluttetMelding deserialiserMelding(String melding) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(melding, OppfølgingAvsluttetMelding.class);
    }
}
