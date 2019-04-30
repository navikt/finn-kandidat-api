package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RequiredArgsConstructor
@RestController
public class TilgangskontrollController {

    private final TilgangskontrollService tilgangskontrollService;
    private final TokenUtils tokenUtils;

    @GetMapping("/harTilgang/{fnr}")
    public boolean harTilgang(
            @PathVariable("fnr") String fnr
    ) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(fnr);
    }

    @GetMapping("/innlogget-bruker")
    public ResponseEntity<Veileder> innloggetBruker() {
        return ResponseEntity.ok(tokenUtils.hentInnloggetVeileder());
    }

}
