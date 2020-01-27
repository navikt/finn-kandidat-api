package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            KandidatDto kandidat,
            Veileder veileder,
            LocalDateTime sistEndret,
            String navKontor
    ) {
        return Kandidat.builder()
                .fnr(kandidat.getFnr())
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

    public List<String> kategorier() {
        ArrayList<String> kategorier = new ArrayList<>();
        ArbeidstidBehov arbeidstidBehov = this.getArbeidstidBehov();
        Set<FysiskBehov> fysiskeBehov = this.getFysiskeBehov();
        Set<ArbeidsmiljøBehov> arbeidsmiljøBehov = this.getArbeidsmiljøBehov();
        Set<GrunnleggendeBehov> grunnleggendeBehov = this.getGrunnleggendeBehov();

        if (arbeidstidBehov != null && !arbeidstidBehov.equals(ArbeidstidBehov.HELTID)) {
            kategorier.add(ArbeidstidBehov.behovskategori);
        }

        if (fysiskeBehov != null && !fysiskeBehov.isEmpty()) {
            kategorier.add(FysiskBehov.behovskategori);
        }

        if (arbeidsmiljøBehov != null && !arbeidsmiljøBehov.isEmpty()) {
            kategorier.add(ArbeidsmiljøBehov.behovskategori);
        }

        if (grunnleggendeBehov != null && !grunnleggendeBehov.isEmpty()) {
            kategorier.add(GrunnleggendeBehov.behovskategori);
        }

        return Collections.unmodifiableList(kategorier);
    }
}
