package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.unleash.FeatureToggleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService.FINN_KANDIDAT_PILOTTILGANG_KONTOR;

@RestController
@Protected
@RequiredArgsConstructor
public class PilottilgangController {

    private final FeatureToggleService featureToggleService;

    @GetMapping("/pilottilgang")
    public ResponseEntity<PilottilgangRespons> hentFeature() {
        boolean harTilgang = featureToggleService.isEnabled(FINN_KANDIDAT_PILOTTILGANG_KONTOR);
        PilottilgangRespons respons = new PilottilgangRespons(harTilgang);
        return ResponseEntity.ok(respons);
    }
}
