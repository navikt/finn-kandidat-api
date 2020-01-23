package no.nav.tag.finnkandidatapi.unleash;

import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
@RequiredArgsConstructor
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    @GetMapping("/features//{feature}")
    public ResponseEntity<Boolean> hentFeature(@PathVariable("feature") String feature) {
        return ResponseEntity.ok(featureToggleService.isEnabled(feature));
    }
}
