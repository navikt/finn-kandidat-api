package no.nav.finnkandidatapi.aktørregister;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentinfoForAktør {

    private List<Identinfo> identer;
    private String feilmelding;
}
