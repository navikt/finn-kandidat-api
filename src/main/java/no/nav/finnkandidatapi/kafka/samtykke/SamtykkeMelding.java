package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
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

    private static final String localDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss[.SSS][xxx]";
    private static final String dateFormat = "yyyy-MM-dd";

    private String aktoerId;
    private String foedselsnummer;
    private String meldingType;
    private String ressurs;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = localDateTimeFormat)
    private LocalDateTime opprettetDato;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = localDateTimeFormat)
    private LocalDateTime slettetDato;
    private Integer versjon;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = localDateTimeFormat)
    private LocalDateTime versjonGjeldendeFra;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    private LocalDate versjonGjeldendeTil;
}
