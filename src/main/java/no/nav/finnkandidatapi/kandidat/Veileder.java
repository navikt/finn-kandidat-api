package no.nav.finnkandidatapi.kandidat;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Veileder {
    private final String navIdent;
    private final String navn;
}
