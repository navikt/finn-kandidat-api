package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.api.Protected;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TilgangskontrollController {

    private final TilgangskontrollService tilgangskontrollService;

    public TilgangskontrollController(TilgangskontrollService tilgangskontrollService) {
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping("/harTilgang/{fnr}")
    @Protected
    public boolean harTilgang(
            @PathVariable("fnr") String fnr
    ) {
        return tilgangskontrollService.harTilgang(fnr);
    }
}
