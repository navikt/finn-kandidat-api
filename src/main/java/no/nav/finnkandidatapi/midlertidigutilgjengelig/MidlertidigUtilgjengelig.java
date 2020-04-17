package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.Builder;
import lombok.Data;
import no.nav.finnkandidatapi.kandidat.Veileder;

import java.time.LocalDateTime;

@Data
@Builder
public class MidlertidigUtilgjengelig {
    private String aktørId;
    private LocalDateTime fraDato;
    private LocalDateTime tilDato;
    private String registrertAvIdent;
    private String registrertAvNavn;
    private String sistEndretAvIdent;
    private String sistEndretAvNavn;

    public static MidlertidigUtilgjengelig opprettMidlertidigUtilgjengelig(MidlertidigUtilgjengeligDto dto, LocalDateTime fraDato, Veileder veileder) {
        return MidlertidigUtilgjengelig.builder()
                .aktørId(dto.getAktørId())
                .fraDato(fraDato)
                .tilDato(dto.getTilDato())
                .registrertAvIdent(veileder.getNavIdent())
                .registrertAvNavn(veileder.getNavn())
                .build();
    }
}
