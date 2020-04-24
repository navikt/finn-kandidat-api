package no.nav.finnkandidatapi.midlertidigutilgjengelig.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;

@Data
@AllArgsConstructor
public class MidlertidigUtilgjengeligSlettet {
    private MidlertidigUtilgjengelig midlertidigUtilgjengelig;
}
