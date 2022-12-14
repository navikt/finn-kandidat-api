package no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;

import java.io.IOException;

public class SisteOppfolgingsperiodeUtils {
    public static SisteOppfolgingsperiodeV1 deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            SisteOppfolgingsperiodeV1 sisteOppfolgingsperiod = mapper.readValue(melding, SisteOppfolgingsperiodeV1.class);

            if (sisteOppfolgingsperiod.getAktorId().isEmpty() || sisteOppfolgingsperiod.getStartDato() == null) {
                throw new RuntimeException("Ugyldig data for siste oppfolging periode på bruker: " + sisteOppfolgingsperiod.getAktorId());
            }
            if (sisteOppfolgingsperiod.getSluttDato() != null && sisteOppfolgingsperiod.getStartDato().isAfter(sisteOppfolgingsperiod.getSluttDato())) {
                throw new RuntimeException("Ugyldig start/slutt dato for siste oppfolging periode på bruker: " + sisteOppfolgingsperiod.getAktorId());
            }
            return sisteOppfolgingsperiod;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere SisteOppfolgingsPeriode", e);
        }
    }
}
