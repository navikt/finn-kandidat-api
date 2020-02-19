package no.nav.finnkandidatapi.kandidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetUtils;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class OppfølgingAvsluttetUtilsTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_aktørId_er_null() {
        OppfølgingAvsluttetMelding meldingUtenAktørId = new OppfølgingAvsluttetMelding(null, new Date());
        OppfølgingAvsluttetUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenAktørId));
    }

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_sluttDato_er_null() {
        OppfølgingAvsluttetMelding meldingUtenSluttDato = new OppfølgingAvsluttetMelding(TestData.enAktørId(), null);
        OppfølgingAvsluttetUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenSluttDato));
    }

    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_ugyldig_json() {
        OppfølgingAvsluttetUtils.deserialiserMelding("ugyldig json");
    }

    @SneakyThrows
    @Test
    public void deserialiserMelding__skal_deserialisere_gyldig_melding() {
        OppfølgingAvsluttetMelding gyldigMelding = new OppfølgingAvsluttetMelding(TestData.enAktørId(), new Date());
        OppfølgingAvsluttetMelding deserialisertMelding = OppfølgingAvsluttetUtils.deserialiserMelding(objectMapper.writeValueAsString(gyldigMelding));
        assertThat(deserialisertMelding).isEqualTo(gyldigMelding);
    }
}
