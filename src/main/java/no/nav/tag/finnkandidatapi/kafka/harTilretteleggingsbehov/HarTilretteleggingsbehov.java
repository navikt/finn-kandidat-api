package no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class HarTilretteleggingsbehov {

    private String aktoerId;
    private boolean harTilretteleggingsbehov;
    private List<String> behov;

    public HarTilretteleggingsbehov(String aktoerId, boolean harTilretteleggingsbehov, List<String> behov) {
        this.aktoerId = aktoerId;
        this.harTilretteleggingsbehov = harTilretteleggingsbehov;
        this.behov = behov == null ? List.of() : behov;
    }
}
