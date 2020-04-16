package no.nav.finnkandidatapi.midlertidigUtilgjengelig;

import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.oidc.api.Protected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Protected
@RestController
@RequestMapping("/midlertidig-utilgjengelig")
public class MidlertidigUtilgjengeligController {
    private final MidlertidigUtilgjengeligService service;
    private final TilgangskontrollService tilgangskontroll;

    public MidlertidigUtilgjengeligController(
            MidlertidigUtilgjengeligService service,
            TilgangskontrollService tilgangskontroll) {
        this.service = service;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<MidlertidigUtilgjengelig> hentMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        MidlertidigUtilgjengelig status = service.hentMidlertidigUtilgjengelig(aktørId, innloggetVeileder);
        return ResponseEntity.ok(status);
    }
}
