package no.nav.tag.finnkandidatapi.metrikker;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.finnkandidatapi.tilbakemelding.Tilbakemelding;

@Data
@AllArgsConstructor
public class TilbakemeldingMottatt {
    private Tilbakemelding tilbakemelding;
}
