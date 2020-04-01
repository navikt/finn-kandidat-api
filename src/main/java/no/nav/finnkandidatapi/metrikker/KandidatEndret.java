package no.nav.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;

import java.util.Optional;

@Data
@AllArgsConstructor
public class KandidatEndret {
    private Kandidat kandidat;
    private Optional<PermittertArbeidssoker> permittertArbeidssoker;
}
