package no.nav.finnkandidatapi.midlertidigUtilgjengelig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengeligDto {
    private String aktørId;
    private LocalDateTime fraDato;
    private LocalDateTime tilDato;
}