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
    private LocalDateTime sistEndretAvVeileder;
    private Set<Arbeidstid> arbeidstid;
    private Set<Fysisk> fysisk;
    private Set<Arbeidshverdagen> arbeidshverdagen;
    private Set<UtfordringerMedNorsk> utfordringerMedNorsk;
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
                .sistEndretAvVeileder(sistEndret)
                .arbeidstid(kandidat.getArbeidstid())
                .fysisk(kandidat.getFysisk())
                .arbeidshverdagen(kandidat.getArbeidshverdagen())
                .utfordringerMedNorsk(kandidat.getUtfordringerMedNorsk())
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
                .sistEndretAvVeileder(sistEndret)
                .sistEndretAv(veileder.getNavIdent())
                .arbeidstid(kandidatDto.getArbeidstid())
                .fysisk(kandidatDto.getFysisk())
                .arbeidshverdagen(kandidatDto.getArbeidshverdagen())
                .utfordringerMedNorsk(kandidatDto.getUtfordringerMedNorsk())
                .navKontor(kandidat.getNavKontor())
                .build();
    }

    public List<String> kategorier() {
        ArrayList<String> kategorier = new ArrayList<>();
        Set<Arbeidstid> arbeidstid = this.getArbeidstid();
        Set<Fysisk> fysisk = this.getFysisk();
        Set<Arbeidshverdagen> arbeidshverdagen = this.getArbeidshverdagen();
        Set<UtfordringerMedNorsk> utfordringerMedNorsk = this.getUtfordringerMedNorsk();

        if (arbeidstid != null && !arbeidstid.isEmpty()) {
            kategorier.add(Arbeidstid.behovskategori);
        }

        if (fysisk != null && !fysisk.isEmpty()) {
            kategorier.add(Fysisk.behovskategori);
        }

        if (arbeidshverdagen != null && !arbeidshverdagen.isEmpty()) {
            kategorier.add(Arbeidshverdagen.behovskategori);
        }

        if (utfordringerMedNorsk != null && !utfordringerMedNorsk.isEmpty()) {
            kategorier.add(UtfordringerMedNorsk.behovskategori);
        }

        return Collections.unmodifiableList(kategorier);
    }
}
