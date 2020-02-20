package no.nav.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class KandidatDto {
    private String fnr;
    private String aktørId;
    private Set<Arbeidstid> arbeidstid;
    private Set<Fysisk> fysisk;
    private Set<Arbeidshverdagen> arbeidshverdagen;
    private Set<UtfordringerMedNorsk> utfordringerMedNorsk;
}
