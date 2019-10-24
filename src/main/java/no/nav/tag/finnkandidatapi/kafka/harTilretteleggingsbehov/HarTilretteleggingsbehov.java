package no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HarTilretteleggingsbehov {
    private String aktoerId;
    private boolean harTilretteleggingsbehov;
}
