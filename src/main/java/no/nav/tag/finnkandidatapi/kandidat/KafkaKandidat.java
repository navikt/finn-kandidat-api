package no.nav.tag.finnkandidatapi.kandidat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaKandidat {
    private boolean slettet;
    private Kandidat kandidat;
}
