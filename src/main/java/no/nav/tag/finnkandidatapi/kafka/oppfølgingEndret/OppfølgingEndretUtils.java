package no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.finnkandidatapi.veilarbarena.Oppfølgingsbruker;

import java.io.IOException;

public class OppfølgingEndretUtils {
    public static Oppfølgingsbruker deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Oppfølgingsbruker oppfølgingEndretMelding = mapper.readValue(melding, Oppfølgingsbruker.class);

            if (oppfølgingEndretMelding.getNavKontor() == null ||oppfølgingEndretMelding.getFnr() == null) {
                throw new RuntimeException("Kunne ikke deserialisere Oppfølgingsbruker, melding: " + oppfølgingEndretMelding);
            }

            return oppfølgingEndretMelding;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere Oppfølgingsbruker", e);
        }
    }
}
