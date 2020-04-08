package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VedtakReplikertMapperTest {


    @Test
    public void skal_parse_localdatetime_med_t_delimiter() throws JsonProcessingException {
        String json = "{\"table\":\"SIAMO.VEDTAK\",\"op_type\":\"I\",\"op_ts\":\"2020-04-07 14:31:08.840468\",\"current_ts\":\"2020-04-07T14:53:03.656001\",\"pos\":\"00000000000000013022\",\"tokens\":{\"FODSELSNR\":\"***********\"},\"after\":{\"VEDTAK_ID\":29501880,\"VEDTAKSTATUSKODE\":\"IVERK\",\"VEDTAKTYPEKODE\":\"E\",\"UTFALLKODE\":\"JA\",\"RETTIGHETKODE\":\"DAGO\",\"PERSON_ID\":4124685,\"FRA_DATO\":\"2018-03-05 00:00:00\",\"TIL_DATO\":null}}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        VedtakReplikert vedtakReplikert = mapper.readValue(json, VedtakReplikert.class);
        assertThat(vedtakReplikert.getOp_type()).isEqualTo("I");
        assertThat(vedtakReplikert.getAfter()).isNotNull();
        assertThat(vedtakReplikert.getAfter().getVedtakstatuskode()).isNotNull();
    }
}
