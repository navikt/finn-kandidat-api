package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SamtykkeMelding {

    private static final String dateFormat = "yyyy-MM-dd";

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
}
