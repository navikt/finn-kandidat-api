package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengelig {
    private Integer id;
    private String aktørId;
    private LocalDateTime fraDato;
    private LocalDateTime tilDato;
    private String registrertAvIdent;
    private String registrertAvNavn;
    private LocalDateTime registreringstidspunkt;
    private boolean slettet;
}
