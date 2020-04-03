package no.nav.finnkandidatapi.kafka.vedtakEndret;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.finnkandidatapi.veilarbarena.Oppfølgingsbruker;

import java.io.IOException;

public class VedtakEndretUtils {
    public static VedtakEndret deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            VedtakEndret vedtakEndret = mapper.readValue(melding, VedtakEndret.class);

            if (vedtakEndret.getOp_type() == null) {
                throw new RuntimeException("Kunne ikke deserialisere VedtakEndret, melding: " + vedtakEndret);
            }
            return vedtakEndret;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere VedtakEndret", e);
        }
    }
}
