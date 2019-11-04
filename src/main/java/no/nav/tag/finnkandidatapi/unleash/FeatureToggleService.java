package no.nav.tag.finnkandidatapi.unleash;

import lombok.RequiredArgsConstructor;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final Unleash unleash;
    private final TokenUtils tokenUtils;

    public boolean isEnabled(String feature) {
        return unleash.isEnabled(feature, contextMedInnloggetBruker());
    }

    private UnleashContext contextMedInnloggetBruker() {
        UnleashContext.Builder builder = UnleashContext.builder();
        if (tokenUtils.harInnloggingsContext()) {
            Veileder veileder = tokenUtils.hentInnloggetVeileder();
            builder.userId(veileder.getNavIdent());
        }

        return builder.build();
    }
}
