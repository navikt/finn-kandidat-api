package no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class OppfølgingAvsluttetUtils {
    public static OppfølgingAvsluttetMelding deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = mapper.readValue(melding, OppfølgingAvsluttetMelding.class);

            if (oppfølgingAvsluttetMelding.getAktørId() == null || oppfølgingAvsluttetMelding.getSluttdato() == null) {
                throw new RuntimeException("Kunne ikke deserialisere OppfølgingAvsluttetMelding, melding: " + oppfølgingAvsluttetMelding);
            }

            return oppfølgingAvsluttetMelding;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
        }
    }
}
