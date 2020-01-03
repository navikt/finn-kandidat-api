package no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov;

import lombok.Value;

import java.util.List;

import static java.util.Arrays.asList;

@Value
public class HarTilretteleggingsbehov {
    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov) {
        this(aktoerId, harTilretteleggingsbehov, List.of());
    }

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov, String... behov) {
        this(aktoerId, harTilretteleggingsbehov, asList(behov));
    }

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov, List<String> behov) {
        this.aktoerId = aktoerId;
        this.harTilretteleggingsbehov = harTilretteleggingsbehov;
        this.behov = behov == null ? List.of() : behov;
    }

    String aktoerId;
    boolean harTilretteleggingsbehov;
    List<String> behov;
}
