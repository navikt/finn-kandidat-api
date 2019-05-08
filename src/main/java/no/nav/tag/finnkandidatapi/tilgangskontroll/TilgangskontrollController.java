package no.nav.tag.finnkandidatapi.tilgangskontroll;

import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.AbacClient;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response.XacmlResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@Unprotected
@RestController
public class TilgangskontrollController {

    private final AbacClient abacClient;

    public TilgangskontrollController(AbacClient abacClient) {
        this.abacClient = abacClient;
    }

    @GetMapping("/abac/ping")
    public XacmlResponse kallAbac() {
        // TODO TAG-363: Bare en testcontroller. Skal fjernes
        return abacClient.ping();
    }

    @GetMapping("/abac/{fnr}/{navIdent}/{action}")
    public XacmlResponse kallAbac(
            @PathParam("fnr") String fnr,
            @PathParam("navIdent") String navIdent,
            @PathParam("action") String action
    ) {
        // TODO TAG-363: Bare en testcontroller. Skal fjernes
        return abacClient.sjekkTilgang(navIdent, fnr, action);
    }
}