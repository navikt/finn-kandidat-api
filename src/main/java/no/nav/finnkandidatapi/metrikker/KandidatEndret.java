package no.nav.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.finnkandidatapi.kandidat.Kandidat;

@Data
@AllArgsConstructor
public class KandidatEndret {
    private Kandidat kandidat;
}
