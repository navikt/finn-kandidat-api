package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VedtakRad {

    private Long vedtak_id;
    private Long sak_id;
    private Long person_id;
    private String vedtaktypekode;
    private String vedtakstatuskode;
    private String utfallkode;
    private String rettighetkode;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime fra_dato;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime til_dato;
    private String fodselsnr;
}
