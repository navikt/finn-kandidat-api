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
    private String aktørId;
    private String sistEndretAv;
    private LocalDateTime sistEndret;
    private ArbeidstidBehov arbeidstidBehov;
    private Set<FysiskBehov> fysiskeBehov;
    private Set<ArbeidsmiljøBehov> arbeidsmiljøBehov;
    private Set<GrunnleggendeBehov> grunnleggendeBehov;
    private String navKontor;

    public static Kandidat opprettKandidat(
            String fnr,
            KandidatDto kandidat,
            Veileder veileder,
            LocalDateTime sistEndret,
            String navKontor
    ) {
        return Kandidat.builder()
                .fnr(fnr)
                .aktørId(kandidat.getAktørId())
                .sistEndretAv(veileder.getNavIdent())
                .sistEndret(sistEndret)
                .arbeidstidBehov(kandidat.getArbeidstidBehov())
                .fysiskeBehov(kandidat.getFysiskeBehov())
                .arbeidsmiljøBehov(kandidat.getArbeidsmiljøBehov())
                .grunnleggendeBehov(kandidat.getGrunnleggendeBehov())
                .navKontor(navKontor)
                .build();
    }

    public static Kandidat endreKandidat(
            Kandidat kandidat,
            KandidatDto kandidatDto,
            Veileder veileder,
            LocalDateTime sistEndret
    ) {
        return Kandidat.builder()
                .id(kandidat.getId())
                .fnr(kandidat.getFnr())
                .aktørId(kandidat.getAktørId())
                .sistEndret(sistEndret)
                .sistEndretAv(veileder.getNavIdent())
                .arbeidstidBehov(kandidatDto.getArbeidstidBehov())
                .fysiskeBehov(kandidatDto.getFysiskeBehov())
                .arbeidsmiljøBehov(kandidatDto.getArbeidsmiljøBehov())
                .grunnleggendeBehov(kandidatDto.getGrunnleggendeBehov())
                .navKontor(kandidat.getNavKontor())
                .build();
    }
}
