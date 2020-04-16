package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengeligDto {
    private String aktørId;
    private LocalDateTime tilDato;
}
