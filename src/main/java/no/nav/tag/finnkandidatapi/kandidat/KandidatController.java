package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController {

    private final KandidatService kandidatService;
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/{aktørId}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("hentKandidat");
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);
        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktørId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping("/eksisterendeaktor/{fnr}")
    public ResponseEntity<Kandidat> eksisterendeAktør(@PathVariable("fnr") String fnr) {
        loggBrukAvEndepunkt("finnesKandidat");
        String aktørId;
        try {
            aktørId = kandidatService.hentAktørId(fnr);
        } catch (FinnKandidatException fe) {
             log.info("Aktør ikke funnet for fnr:" + fnr, fe);
            throw new NotFoundException("Aktør mangler");
        }

        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);

        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktørId)
                .orElse(Kandidat.builder().aktørId(aktørId).build());
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        loggBrukAvEndepunkt("hentKandidater");
        List<Kandidat> kandidater = kandidatService.hentKandidater().stream()
                .filter(kandidat -> tilgangskontroll.harLesetilgangTilKandidat(kandidat.getAktørId()))
                .collect(Collectors.toList());

        kandidater.stream()
                .filter(kandidat -> StringUtils.isBlank(kandidat.getAktørId()))
                .forEach(kandidat -> kandidat.setAktørId(kandidatService.hentAktørId(kandidat.getFnr())));
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody Kandidat kandidat) {
        loggBrukAvEndepunkt("opprettKandidat");
        String aktørId = kandidat.getAktørId();
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktørId);

        String fnr = kandidatService.hentFnr(aktørId);

        kandidat.setFnr(fnr);

        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat opprettetKandidat = kandidatService.opprettKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(opprettetKandidat);
    }

    @PutMapping
    public ResponseEntity<Kandidat> endreKandidat(@RequestBody Kandidat kandidat) {
        loggBrukAvEndepunkt("endreKandidat");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat endretKandidat = kandidatService.endreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(endretKandidat);
    }

    @GetMapping("/{aktørId}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("hentSkrivetilgang");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktørId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{aktørId}")
    public ResponseEntity slettKandidat(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("slettKandidat");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktørId);

        Optional<Integer> id = kandidatService.slettKandidat(aktørId, tilgangskontroll.hentInnloggetVeileder());

        if (id.isEmpty()) {
            throw new NotFoundException();
        }

        return ResponseEntity.ok().build();
    }

    private void loggBrukAvEndepunkt(String endepunkt) {
        log.info(
                "Bruker med ident {} kaller endepunktet {}.",
                tilgangskontroll.hentInnloggetVeileder().getNavIdent(),
                endepunkt
        );
    }

}
