package no.nav.tag.finnkandidatapi.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Kandidatendring {
    private String aktoerId;
    private boolean harTilretteleggingsbehov;
}
