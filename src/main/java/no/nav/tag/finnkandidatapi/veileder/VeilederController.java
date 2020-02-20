package no.nav.tag.finnkandidatapi.veileder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Slf4j
@Protected
@RestController
@RequestMapping("/veileder")
@RequiredArgsConstructor
public class VeilederController {
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/me")
    public ResponseEntity<String> hentInnloggetVeileder() {
        loggBrukAvEndepunkt("innloggetVeileder");
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();

        return ResponseEntity.ok(veileder.getNavIdent());
    }

    private void loggBrukAvEndepunkt(String endepunkt) {
        log.info(
                "Bruker med ident {} henter innlogget veileder.",
                tilgangskontroll.hentInnloggetVeileder().getNavIdent()
        );
    }
}
