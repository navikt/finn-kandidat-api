package no.nav.tag.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.SlettKandidat;

@Data
@AllArgsConstructor
public class KandidatSlettet {
    private SlettKandidat slettKandidat;
    private Integer id;
}
