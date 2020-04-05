package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class VedtakReplikertUtils {
    public static VedtakReplikert deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            VedtakReplikert vedtakReplikert = mapper.readValue(melding, VedtakReplikert.class);

            if (vedtakReplikert.getOp_type() == null) {
                throw new RuntimeException("Kunne ikke deserialisere VedtakRepliker, melding: " + vedtakReplikert);
            }
            return vedtakReplikert;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere VedtakRepliker", e);
        }
    }
}
