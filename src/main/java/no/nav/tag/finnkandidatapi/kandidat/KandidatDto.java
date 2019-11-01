package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class KandidatDto {
    private String aktørId;
    private ArbeidstidBehov arbeidstidBehov;
    private Set<FysiskBehov> fysiskeBehov;
    private Set<ArbeidsmiljøBehov> arbeidsmiljøBehov;
    private Set<GrunnleggendeBehov> grunnleggendeBehov;
}