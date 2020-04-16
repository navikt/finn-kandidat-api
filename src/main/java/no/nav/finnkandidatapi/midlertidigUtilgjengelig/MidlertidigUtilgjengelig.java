package no.nav.finnkandidatapi.midlertidigUtilgjengelig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengelig {
    private int id;
    private String aktørId;
    private LocalDateTime fraDato;
    private LocalDateTime tilDato;
    private String registrertAv;
    private LocalDateTime registreringstidspunkt;
}
