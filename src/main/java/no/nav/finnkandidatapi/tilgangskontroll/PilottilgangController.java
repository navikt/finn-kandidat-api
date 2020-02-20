package no.nav.finnkandidatapi.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import no.nav.security.oidc.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
@RequiredArgsConstructor
public class PilottilgangController {

    public static final String REGISTRER_TILRETTELEGGINGSBEHOV = "finnkandidat.pilottilgang.registrer-tilretteleggingsbehov";

    private final FeatureToggleService featureToggleService;

    @GetMapping("/pilottilgang")
    public ResponseEntity<PilottilgangRespons> harPilottilgang() {
        boolean harTilgang = featureToggleService.isEnabled(REGISTRER_TILRETTELEGGINGSBEHOV);
        PilottilgangRespons respons = new PilottilgangRespons(harTilgang);
        return ResponseEntity.ok(respons);
    }
}
