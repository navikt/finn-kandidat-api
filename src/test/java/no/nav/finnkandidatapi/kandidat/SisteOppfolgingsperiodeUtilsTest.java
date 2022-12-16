package no.nav.finnkandidatapi.kandidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode.SisteOppfolgingsperiodeUtils;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.UUID;

import static no.nav.finnkandidatapi.TestData.enAktørId;
import static org.assertj.core.api.Assertions.assertThat;

public class SisteOppfolgingsperiodeUtilsTest {
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_aktørId_er_null() {
        SisteOppfolgingsperiodeV1 meldingUtenAktørId = new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                null,
                ZonedDateTime.now().minusYears(2),
                ZonedDateTime.now());
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenAktørId));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_startDato_er_null() {
        SisteOppfolgingsperiodeV1 meldingUtenSluttDato = new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                enAktørId(),
                null,
                ZonedDateTime.now());
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenSluttDato));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_startDato_er_etter_sluttDato() {
        SisteOppfolgingsperiodeV1 meldingUtenSluttDato = new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                enAktørId(),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusYears(2));
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenSluttDato));
    }

    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_ugyldig_json() {
        SisteOppfolgingsperiodeUtils.deserialiserMelding("ugyldig json");
    }

    @SneakyThrows
    @Test
    public void deserialiserMelding__skal_deserialisere_gyldig_melding() {
        SisteOppfolgingsperiodeV1 gyldigMelding = new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                enAktørId(),
                ZonedDateTime.now().minusYears(2),
                ZonedDateTime.now());
        SisteOppfolgingsperiodeV1 deserialisertMelding = SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(gyldigMelding));
        assertThat(deserialisertMelding).isEqualTo(gyldigMelding);
    }
}
