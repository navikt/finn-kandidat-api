package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public ResponseEntity<?> getMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId) {
        tilgangskontroll.hentInnloggetVeileder();

        Optional<MidlertidigUtilgjengelig> status = service.hentMidlertidigUtilgjengelig(aktørId);

        return status.isEmpty() ?
             ResponseEntity.notFound().build() : ResponseEntity.ok(status.get());
    }

    @PostMapping
    public ResponseEntity<MidlertidigUtilgjengelig> postMidlertidigUtilgjengelig(@RequestBody MidlertidigUtilgjengeligDto midlertidigUtilgjengelig) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        MidlertidigUtilgjengelig lagretMidlertidigUtilgjengelig = service.opprettMidlertidigUtilgjengelig(midlertidigUtilgjengelig, innloggetVeileder);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagretMidlertidigUtilgjengelig);
    }
}
