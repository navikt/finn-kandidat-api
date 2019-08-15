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

    @GetMapping("/{aktorId}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("aktorId") String aktorId) {
        loggBrukAvEndepunkt("hentKandidat");
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktorId);
        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktorId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping("/eksisterendeaktor/{fnr}")
    public ResponseEntity<Kandidat> eksisterendeAktør(@PathVariable("fnr") String fnr) {
        loggBrukAvEndepunkt("finnesKandidat");
        String aktorId;
        try {
            aktorId = kandidatService.hentAktorId(fnr);
        } catch (FinnKandidatException fe) {
             log.info("Aktør ikke funnet for fnr:" + fnr, fe);
            throw new NotFoundException("Aktør mangler");
        }

        tilgangskontroll.sjekkLesetilgangTilKandidat(aktorId);

        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktorId)
                .orElse(Kandidat.builder().aktorId(aktorId).build());
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        loggBrukAvEndepunkt("hentKandidater");
        List<Kandidat> kandidater = kandidatService.hentKandidater().stream()
                .filter(kandidat -> tilgangskontroll.harLesetilgangTilKandidat(kandidat.getAktorId()))
                .collect(Collectors.toList());

        kandidater.stream()
                .filter(kandidat -> StringUtils.isBlank(kandidat.getAktorId()))
                .forEach(kandidat -> kandidat.setAktorId(kandidatService.hentAktorId(kandidat.getFnr())));
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody Kandidat kandidat) {
        loggBrukAvEndepunkt("opprettKandidat");
        String aktorId = kandidat.getAktorId();
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktorId);

        String fnr = kandidatService.hentFnr(aktorId);

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
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getAktorId());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat endretKandidat = kandidatService.endreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(endretKandidat);
    }

    @GetMapping("/{aktorId}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("aktorId") String aktorId) {
        loggBrukAvEndepunkt("hentSkrivetilgang");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{aktorId}")
    public ResponseEntity slettKandidat(@PathVariable("aktorId") String aktorId) {
        loggBrukAvEndepunkt("slettKandidat");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktorId);

        Optional<Integer> id = kandidatService.slettKandidat(aktorId, tilgangskontroll.hentInnloggetVeileder());

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
