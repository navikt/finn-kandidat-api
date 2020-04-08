package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class VedtakReplikertUtils {
    public static VedtakReplikert deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            VedtakReplikert vedtakReplikert = mapper.readValue(melding, VedtakReplikert.class);

            if (vedtakReplikert.getOp_type() == null) {
                throw new RuntimeException("Kunne ikke deserialisere VedtakReplikert, melding: " + vedtakReplikert);
            }
            return vedtakReplikert;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere VedtakReplikert", e);
        }
    }
}
