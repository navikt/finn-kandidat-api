package no.nav.tag.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;

@Data
@AllArgsConstructor
public class KandidatOpprettet {
    private Kandidat kandidat;
}
