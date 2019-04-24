package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;

public class TilgangskontrollController {

    private final TilgangskontrollService tilgangskontrollService;

    public TilgangskontrollController(TilgangskontrollService tilgangskontrollService) {
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping("/harTilgang")
    @Protected
    public boolean harTilgang() {
        return tilgangskontrollService.harTilgang();
    }
}
