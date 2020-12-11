package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SamtykkeMelding {

    private static final String format = "yyyy-MM-dd'T'HH:mm:ss[xxx]";

    private String aktoerId;
    private String foedselsnummer;
    private String meldingType;
    private String ressurs;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = format)
    private LocalDateTime opprettetDato;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = format)
    private LocalDateTime slettetDato;
    private Integer versjon;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = format)
    private LocalDateTime versjonGjeldendeFra;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = format)
    private LocalDateTime versjonGjeldendeTil;
}
