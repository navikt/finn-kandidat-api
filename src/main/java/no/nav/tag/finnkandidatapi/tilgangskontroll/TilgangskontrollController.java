package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
public class TilgangskontrollController {

    private final VeilarbabacClient veilarbabacClient;

    public TilgangskontrollController(VeilarbabacClient veilarbabacClient) {
        this.veilarbabacClient = veilarbabacClient;
    }

    @GetMapping("/veilarbabac/{fnr}/{navIdent}/{action}")
    public boolean kallAbac(
            @PathVariable("fnr") String fnr,
            @PathVariable("navIdent") String navIdent,
            @PathVariable("action") String action
    ) {
        // TODO TAG-363: Bare en testcontroller. Skal fjernes
        return veilarbabacClient.sjekkTilgang(
                new Veileder(navIdent),
                fnr,
                TilgangskontrollAction.valueOf(action.toLowerCase())
        );
    }
}
