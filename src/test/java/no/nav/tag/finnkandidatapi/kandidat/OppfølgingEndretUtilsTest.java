package no.nav.tag.finnkandidatapi.kandidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret.OppfølgingEndretUtils;
import no.nav.tag.finnkandidatapi.veilarbarena.Oppfølgingsbruker;
import org.junit.Test;

import static no.nav.tag.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

public class OppfølgingEndretUtilsTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_fnr_er_null() {
        Oppfølgingsbruker meldingUtenFnr = new Oppfølgingsbruker(null, etNavKontor());
        OppfølgingEndretUtils.deserialiserMelding(objectMapper.writeValueAsString(meldingUtenFnr));
    }

    @SneakyThrows
    @Test
    public void deserialiserMelding__skal_fungere_hvis_navkontor_er_null() {
        Oppfølgingsbruker meldingUtenNavKontor = new Oppfølgingsbruker(etFnr(), null);
        Oppfølgingsbruker deserialisert = OppfølgingEndretUtils
                .deserialiserMelding(objectMapper.writeValueAsString(meldingUtenNavKontor));
        assertThat(deserialisert).isEqualTo(meldingUtenNavKontor);
    }

    @Test(expected = RuntimeException.class)
    public void deserialiserMelding__skal_kaste_runtime_exception_hvis_ugyldig_json() {
        OppfølgingEndretUtils.deserialiserMelding("ugyldig json");
    }

    @SneakyThrows
    @Test
    public void deserialiserMelding__skal_deserialisere_gyldig_melding() {
        Oppfølgingsbruker oppfølgingsbruker = enOppfølgingsbruker();
        Oppfølgingsbruker deserialisertMelding = OppfølgingEndretUtils
                .deserialiserMelding(objectMapper.writeValueAsString(oppfølgingsbruker));
        assertThat(deserialisertMelding).isEqualTo(oppfølgingsbruker);
    }
}
