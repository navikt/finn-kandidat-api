package no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;

import java.io.IOException;
import java.util.TimeZone;

public class SisteOppfolgingsperiodeUtils {
    public static SisteOppfolgingsperiodeV1 deserialiserMelding(String melding) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));
            SisteOppfolgingsperiodeV1 sisteOppfolgingsperiode = mapper.readValue(melding, SisteOppfolgingsperiodeV1.class);

            if (sisteOppfolgingsperiode.getAktorId().isEmpty() || sisteOppfolgingsperiode.getStartDato() == null) {
                throw new RuntimeException("Ugyldig data for siste oppfolgingsperiode på bruker: " + sisteOppfolgingsperiode.getAktorId());
            }
            if (sisteOppfolgingsperiode.getSluttDato() != null && sisteOppfolgingsperiode.getStartDato().isAfter(sisteOppfolgingsperiode.getSluttDato())) {
                throw new RuntimeException("Ugyldig start/slutt dato for siste oppfolgingsperiode på bruker: " + sisteOppfolgingsperiode.getAktorId());
            }
            return sisteOppfolgingsperiode;

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere SisteOppfolgingsPeriode", e);
        }
    }
}
