package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;
import no.nav.tag.finnkandidatapi.DateProvider;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class Kandidat {
    private Integer id;
    private String fnr;
    private String aktørId;
    private String sistEndretAv;
    private LocalDateTime sistEndret;
    private ArbeidstidBehov arbeidstidBehov;
    private Set<FysiskBehov> fysiskeBehov;
    private Set<ArbeidsmiljøBehov> arbeidsmiljøBehov;
    private Set<GrunnleggendeBehov> grunnleggendeBehov;
    private String navKontor;

    public static Kandidat endreKandidat(
            Kandidat kandidat,
            Kandidatendring kandidatendring,
            Veileder veileder,
            LocalDateTime sistEndret
    ) {
        return Kandidat.builder()
                .id(kandidat.getId())
                .fnr(kandidat.getFnr())
                .aktørId(kandidat.getAktørId())
                .sistEndret(sistEndret)
                .sistEndretAv(veileder.getNavIdent())
                .arbeidstidBehov(kandidatendring.getArbeidstidBehov())
                .fysiskeBehov(kandidatendring.getFysiskeBehov())
                .arbeidsmiljøBehov(kandidatendring.getArbeidsmiljøBehov())
                .grunnleggendeBehov(kandidatendring.getGrunnleggendeBehov())
                .navKontor(kandidat.getNavKontor())
                .build();
    }
}
