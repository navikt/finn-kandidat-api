package no.nav.finnkandidatapi.kafka.vedtakEndret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class VedtakEndretCustomDateDeserializerTest {

    @Test
    public void skal_parse_localdatetime_med_t_delimiter() throws JsonProcessingException {
        String json = "{ \"tid\": \"2013-06-02T22:14:41.000001\" }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNotNull();
        assertThat(value.getTid().getYear()).isEqualTo(2013);
        assertThat(value.getTid().getMonth()).isEqualTo(Month.JUNE);
        assertThat(value.getTid().getDayOfMonth()).isEqualTo(2);
        assertThat(value.getTid().getHour()).isEqualTo(22);
        assertThat(value.getTid().getMinute()).isEqualTo(14);
        assertThat(value.getTid().getSecond()).isEqualTo(41);
        assertThat(value.getTid().getNano()).isEqualTo(1000);
    }


    @Test
    public void skal_parse_localdatetime_uten_t_delimiter() throws JsonProcessingException {
        String json = "{ \"tid\": \"2013-06-02 22:14:41.000001\" }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNotNull();
        assertThat(value.getTid().getYear()).isEqualTo(2013);
        assertThat(value.getTid().getMonth()).isEqualTo(Month.JUNE);
        assertThat(value.getTid().getDayOfMonth()).isEqualTo(2);
        assertThat(value.getTid().getHour()).isEqualTo(22);
        assertThat(value.getTid().getMinute()).isEqualTo(14);
        assertThat(value.getTid().getSecond()).isEqualTo(41);
        assertThat(value.getTid().getNano()).isEqualTo(1000);
    }

    @Test
    public void skal_parse_localdatetime_uten_t_delimiter_og_uten_fraksjoner() throws JsonProcessingException {
        String json = "{ \"tid\": \"1995-12-31:15:00:00\" }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNotNull();
        assertThat(value.getTid().getYear()).isEqualTo(1995);
        assertThat(value.getTid().getMonth()).isEqualTo(Month.DECEMBER);
        assertThat(value.getTid().getDayOfMonth()).isEqualTo(31);
        assertThat(value.getTid().getHour()).isEqualTo(15);
        assertThat(value.getTid().getMinute()).isEqualTo(0);
        assertThat(value.getTid().getSecond()).isEqualTo(0);
        assertThat(value.getTid().getNano()).isEqualTo(0);
    }

    @Test
    public void skal_parse_manglende_felt_til_null() throws JsonProcessingException {
        String json = "{ }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNull();
    }

    @Test
    public void skal_parse_null_til_null() throws JsonProcessingException {
        String json = "{ \"tid\": null }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNull();
    }

    @Test
    public void skal_parse_tomt_felt_til_null() throws JsonProcessingException {
        String json = "{ \"tid\": \"\" }";
        ObjectMapper mapper = new ObjectMapper();

        ObjektMedLocalDateTime value = mapper.readValue(json, ObjektMedLocalDateTime.class);
        assertThat(value.getTid()).isNull();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ObjektMedLocalDateTime {

        @JsonDeserialize(using = VedtakEndretCustomDateDeserializer.class)
        private LocalDateTime tid;
    }
}
