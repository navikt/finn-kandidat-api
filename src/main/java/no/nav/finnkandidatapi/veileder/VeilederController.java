package no.nav.finnkandidatapi.veileder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated finn-kandidat frontend er slått av så endepunktet blir ikke brukt lengre
 */
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