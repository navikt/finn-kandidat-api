package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class Kandidat {
    private Integer id;
    private String fnr;
    private String sistEndretAv;
    private LocalDateTime sistEndret;
    private ArbeidstidBehov arbeidstidBehov;
    private Set<FysiskBehov> fysiskeBehov;
    private Set<ArbeidsmiljoBehov> arbeidsmiljoBehov;
    private Set<GrunnleggendeBehov> grunnleggendeBehov;
}
