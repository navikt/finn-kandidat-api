package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Protected
@RequestMapping("/tilgangskontroll")
@RestController
public class TilgangskontrollController {

    private final TokenUtils tokenUtils;

    public TilgangskontrollController(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("/innloggetBruker")
    public String innloggetBruker() {
        return tokenUtils.hentIssuer();
    }
}
