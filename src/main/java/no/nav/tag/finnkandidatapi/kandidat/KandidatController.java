package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController {

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
        List<Kandidat> kandidater = kandidatService.hentKandidater().stream()
                .filter(kandidat -> tilgangskontroll.harLesetilgangTilKandidat(kandidat.getFnr()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody Kandidat kandidat) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getFnr());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat opprettetKandidat = kandidatService.opprettKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(opprettetKandidat);
    }

    @PutMapping
    public ResponseEntity<Kandidat> endreKandidat(@RequestBody Kandidat kandidat) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getFnr());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat endretKandidat = kandidatService.endreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(endretKandidat);
    }

    @GetMapping("/{fnr}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fnr}")
    public ResponseEntity slettKandidat(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);

        if (kandidatService.hentNyesteKandidat(fnr).isEmpty()) {
            throw new NotFoundException();
        }

        kandidatService.slettKandidat(fnr, tilgangskontroll.hentInnloggetVeileder());

        return ResponseEntity.ok().build();
    }

}
