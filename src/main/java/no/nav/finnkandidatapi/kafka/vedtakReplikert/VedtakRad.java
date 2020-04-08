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
public class VedtakRad {

    private Long vedtak_id;
    private Long person_id;
    private String vedtaktypekode;
    private String vedtakstatuskode;
    private String utfallkode;
    private String rettighetkode;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime fra_dato;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime til_dato;

    public boolean erPermitteringsvedtak() {
        return rettighetkode != null && (rettighetkode.equalsIgnoreCase("FISK") || rettighetkode.equalsIgnoreCase("PERM"));
    }
}
