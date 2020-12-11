package no.nav.finnkandidatapi.samtykke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Samtykke {
    private String aktoerId;
    private String gjelder;
    private String endring;
}
