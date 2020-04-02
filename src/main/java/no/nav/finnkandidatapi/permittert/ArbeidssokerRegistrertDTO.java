package no.nav.finnkandidatapi.permittert;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ArbeidssokerRegistrertDTO {

    private String aktørId;
    private String status;
    private LocalDateTime registreringTidspunkt;

}
