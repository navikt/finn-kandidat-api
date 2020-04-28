package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import lombok.Builder;
import lombok.Data;
import no.nav.finnkandidatapi.kandidat.Veileder;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public static final String TILGJENGELIG_INNEN_1_UKE = "tilgjengeliginnen1uke";
    public static final String MIDLERTIDIG_UTILGJENGELIG = "midlertidigutilgjengelig";

    public static MidlertidigUtilgjengelig opprettMidlertidigUtilgjengelig(MidlertidigUtilgjengeligInboundDto dto, LocalDateTime fraDato, Veileder veileder) {
        return MidlertidigUtilgjengelig.builder()
                .aktørId(dto.getAktørId())
                .fraDato(fraDato)
                .tilDato(dto.getTilDato())
                .registrertAvIdent(veileder.getNavIdent())
                .registrertAvNavn(veileder.getNavn())
                .build();
    }

    public static Optional<String> finnMidlertidigUtilgjengeligFilter(Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig) {
        if (midlertidigUtilgjengelig.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime tilDato = midlertidigUtilgjengelig.get().getTilDato();
        LocalDateTime nå = LocalDateTime.now();
        if (tilDato == null) {
            return Optional.empty();
        } else if (nå.toLocalDate().isAfter(tilDato.toLocalDate())) {
            return Optional.empty();
        } else if (nå.toLocalDate().isAfter(tilDato.toLocalDate().minusWeeks(1))) {
            return Optional.of(TILGJENGELIG_INNEN_1_UKE);
        } else {
            return Optional.of(MIDLERTIDIG_UTILGJENGELIG);
        }
    }
}
