package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.LocalDate;
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

    @JsonDeserialize(using = ExtractNumbersDeserializer.class)
    private String aktoerId;
    private String foedselsnummer;
    private String meldingType;
    private String ressurs;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime opprettetDato;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime slettetDato;
    private Integer versjon;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    private LocalDate versjonGjeldendeFra;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    private LocalDate versjonGjeldendeTil;


    public SamtykkeMelding(String jsonMelding) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SamtykkeMelding samtykkeMelding = mapper.readValue(jsonMelding, SamtykkeMelding.class);
            BeanUtils.copyProperties(samtykkeMelding, this);
            validerSamtykkeMelding(samtykkeMelding);
            log.info("Samtykkemelding:"
                    + " Opprettet dato:" + samtykkeMelding.getOpprettetDato()
                    + " Slettet dato:" + samtykkeMelding.getSlettetDato()
                    + " Meldingstype:" + samtykkeMelding.getMeldingType()
                    + " Ressurs:" + samtykkeMelding.getRessurs());

        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke deserialisere samtykkemelding", e);
        }
    }

    private void validerSamtykkeMelding(SamtykkeMelding samtykkeMelding) {
        if (StringUtils.isBlank(samtykkeMelding.getAktoerId())) {
            throw new RuntimeException("AktørID mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getMeldingType())) {
            throw new RuntimeException("Meldingtype mangler");
        }

        if (StringUtils.isBlank(samtykkeMelding.getRessurs())) {
            throw new RuntimeException("Ressurs mangler");
        }

        if (samtykkeMelding.getOpprettetDato() == null && samtykkeMelding.getSlettetDato() == null) {
            throw new RuntimeException("OpprettetDato eller Slettetdato må ha verdi");
        }
    }


}
