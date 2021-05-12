package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class SamtykkeMelding {

    private static final String dateFormat = "yyyy-MM-dd";

    @JsonDeserialize(using = ExtractNumbersFromStringDeserializer.class)
    private String aktoerId;

    private String fnr;

    private String ressurs;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime opprettetDato;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime slettetDato;

    public SamtykkeMelding(String jsonMelding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SamtykkeMelding samtykkeMelding = mapper.readValue(jsonMelding, SamtykkeMelding.class);
            BeanUtils.copyProperties(samtykkeMelding, this);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere samtykkemelding", e);
        }
    }

    public String getMeldingType() {
        if(slettetDato != null)
            return "SAMTYKKE_SLETTET";
        else
            return "SAMTYKKE_OPPRETTET";
    }

}
