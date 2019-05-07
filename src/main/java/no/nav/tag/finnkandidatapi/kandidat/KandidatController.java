package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KandidatService kandidatService;
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/{fnr}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkLesetilgangTilKandidat(fnr);

        Kandidat kandidat = kandidatService.hentNyesteKandidat(fnr).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        List<Kandidat> kandidater = kandidatService.hentKandidater();
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(@RequestBody Kandidat kandidat) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getFnr());

        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat lagretKandidat = kandidatService.lagreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        applicationEventPublisher.publishEvent(lagretKandidat);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }

    @GetMapping("/{fnr}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("fnr") String fnr) {
        boolean harSkrivetilgang = tilgangskontroll.harSkrivetilgangTilKandidat(fnr);
        if (harSkrivetilgang) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
