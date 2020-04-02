package no.nav.finnkandidatapi.permittert;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PermittertArbeidssoker {

    public static final String ER_PERMITTERT_KATEGORI = "permittert";

    private Integer id;
    private String aktørId;

    private String statusFraVeilarbRegistrering;
    private LocalDateTime tidspunktForStatusFraVeilarbRegistrering;

    public static PermittertArbeidssoker opprettPermittertArbeidssoker(
            ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO
    ) {
        return PermittertArbeidssoker.builder()
                .aktørId(arbeidssokerRegistrertDTO.getAktørId())
                .statusFraVeilarbRegistrering(arbeidssokerRegistrertDTO.getStatus())
                .tidspunktForStatusFraVeilarbRegistrering(arbeidssokerRegistrertDTO.getRegistreringTidspunkt())
                .build();
    }

    public static PermittertArbeidssoker endrePermittertArbeidssoker(
            PermittertArbeidssoker permittertArbeidssoker,
            ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO
    ) {
        return PermittertArbeidssoker.builder()
                .id(permittertArbeidssoker.getId())
                .aktørId(permittertArbeidssoker.getAktørId())
                .statusFraVeilarbRegistrering(arbeidssokerRegistrertDTO.getStatus())
                .tidspunktForStatusFraVeilarbRegistrering(arbeidssokerRegistrertDTO.getRegistreringTidspunkt())
                .build();
    }

    public boolean erPermittert() {
        return statusFraVeilarbRegistrering.equalsIgnoreCase(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name());
    }
}
