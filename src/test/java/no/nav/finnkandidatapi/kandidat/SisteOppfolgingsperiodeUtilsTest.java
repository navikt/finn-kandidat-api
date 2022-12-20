package no.nav.finnkandidatapi.kandidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode.SisteOppfolgingsperiodeUtils;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import org.junit.Test;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static no.nav.finnkandidatapi.TestData.enAktørId;
import static org.assertj.core.api.Assertions.assertThat;

public class SisteOppfolgingsperiodeUtilsTest {
    private final ObjectMapper objectMapper = SisteOppfolgingsperiodeUtils.objectMapper;

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_aktørId_er_null() {
        SisteOppfolgingsperiodeV1 meldingUtenAktørId = new SisteOppfolgingsperiodeV1(randomUUID(), null, now().minusYears(2), now());
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenAktørId));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_startDato_er_null() {
        SisteOppfolgingsperiodeV1 meldingUtenSluttDato = new SisteOppfolgingsperiodeV1(randomUUID(), enAktørId(), null, now());
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenSluttDato));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_startDato_er_etter_sluttDato() {
        SisteOppfolgingsperiodeV1 meldingUtenSluttDato = new SisteOppfolgingsperiodeV1(randomUUID(), enAktørId(), now(), now().minusYears(2));
        SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenSluttDato));
    }

    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_ugyldig_json() {
        SisteOppfolgingsperiodeUtils.deserialiserMelding("ugyldig json");
    }

    @SneakyThrows
    @Test
    public void deserialiserMelding__skal_deserialisere_gyldig_melding() {
        SisteOppfolgingsperiodeV1 expected = new SisteOppfolgingsperiodeV1(randomUUID(), enAktørId(), now().minusYears(2), now());
        SisteOppfolgingsperiodeV1 actual = SisteOppfolgingsperiodeUtils.deserialiserMelding(objectMapper.writeValueAsString(expected));
        assertThat(actual.getAktorId()).isEqualTo(expected.getAktorId());
        assertThat(actual.getUuid()).isEqualTo(expected.getUuid());
        assertThat(actual.getStartDato()).isEqualTo(expected.getStartDato());
        assertThat(actual.getSluttDato()).isEqualTo(expected.getSluttDato());
    }
}
