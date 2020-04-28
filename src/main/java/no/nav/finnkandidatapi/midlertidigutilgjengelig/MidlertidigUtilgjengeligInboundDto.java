package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengeligInboundDto {
    private String aktørId;
    private LocalDateTime tilDato;
}
