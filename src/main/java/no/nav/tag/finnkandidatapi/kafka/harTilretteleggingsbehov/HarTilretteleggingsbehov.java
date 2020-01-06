package no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@EqualsAndHashCode
@ToString
public class HarTilretteleggingsbehov {
    /**
     * Needed for JSON deserializing
     */
    private HarTilretteleggingsbehov() {
    }

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov) {
        this(aktoerId, harTilretteleggingsbehov, List.of());
    }

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov, String... behov) {
        this(aktoerId, harTilretteleggingsbehov, Arrays.asList(behov));
    }

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov, List<String> behov) {
        this.aktoerId = aktoerId;
        this.harTilretteleggingsbehov = harTilretteleggingsbehov;
        this.behov = behov == null ? List.of() : behov;
    }

    private String aktoerId;
    private boolean harTilretteleggingsbehov;
    private List<String> behov;
}
