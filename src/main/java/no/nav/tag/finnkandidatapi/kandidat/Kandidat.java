package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Kandidat {
    private Integer id;
    private String fnr;
    private String sistEndretAv;
    private LocalDateTime sistEndret;
    private ArbeidstidBehov arbeidstidBehov;
    private List<FysiskBehov> fysiskeBehov;
    private List<ArbeidsmiljoBehov> arbeidsmiljoBehov;
    private List<GrunnleggendeBehov> grunnleggendeBehov;
}
