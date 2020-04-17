package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import com.sun.mail.iap.Response;
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

        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = service.hentMidlertidigUtilgjengelig(aktørId);

        return midlertidigUtilgjengelig.isEmpty() ?
             ResponseEntity.notFound().build() : ResponseEntity.ok(midlertidigUtilgjengelig.get());
    }

    @PostMapping
    public ResponseEntity<MidlertidigUtilgjengelig> postMidlertidigUtilgjengelig(@RequestBody MidlertidigUtilgjengeligDto midlertidigUtilgjengelig) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        MidlertidigUtilgjengelig lagret = service.opprettMidlertidigUtilgjengelig(midlertidigUtilgjengelig, innloggetVeileder);
        return ResponseEntity.status(HttpStatus.CREATED).body(lagret);
    }

    @PutMapping("/{aktørId}")
    public ResponseEntity<MidlertidigUtilgjengelig> putMidlertidigUtilgjengelig(@PathVariable("aktørId") String aktørId, @RequestBody MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto) {
        Veileder innloggetVeileder = tilgangskontroll.hentInnloggetVeileder();

        if (!aktørId.equals(midlertidigUtilgjengeligDto.getAktørId())) {
            return ResponseEntity.badRequest().build();
        }

        Optional<MidlertidigUtilgjengelig> forlenget = service.forlengeMidlertidigUtilgjengelig(aktørId, midlertidigUtilgjengeligDto.getTilDato(), innloggetVeileder);

        return forlenget.isEmpty() ?
                ResponseEntity.notFound().build() : ResponseEntity.ok(forlenget.get());
    }

    @DeleteMapping("/{aktørId}")
    public ResponseEntity<MidlertidigUtilgjengelig> deleteMidlertidigUtilgjenglig(@PathVariable("aktørId") String aktørId) {
        tilgangskontroll.hentInnloggetVeileder();

        service.slettMidlertidigUtilgjengelig(aktørId);
        return ResponseEntity.ok().build();
    }
}
