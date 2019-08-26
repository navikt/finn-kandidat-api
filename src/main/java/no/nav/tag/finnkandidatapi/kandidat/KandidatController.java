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

import static no.bekk.bekkopen.person.FodselsnummerValidator.isValid;

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

    @GetMapping("/{fnr}/aktorId")
    public ResponseEntity<String> hentAktørId(@PathVariable("fnr") String fnr) {
        loggBrukAvEndepunkt("hentAktørId");

        boolean gyldigFnr = isValid(fnr);
        if (!gyldigFnr) {
            return ResponseEntity.badRequest().body("Ugyldig fødselsnummer");
        }

        String aktørId = kandidatService.hentAktørId(fnr);
        return ResponseEntity.ok(aktørId);
    }

    @GetMapping("/{aktørId}/fnr")
    public ResponseEntity<String> hentFnr(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("hentFnr");
        String fnr = kandidatService.hentFnr(aktørId);
        return ResponseEntity.ok(fnr);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        loggBrukAvEndepunkt("hentKandidater");
        List<Kandidat> kandidater = kandidatService.hentKandidater().stream()
                .filter(kandidat -> tilgangskontroll.harLesetilgangTilKandidat(kandidat.getAktørId()))
                .collect(Collectors.toList());

        // TODO: Fjerne denne når vi vet at alle kandidater har en aktørId?
        //  Her knytter oss hardt mot aktørregisteret for hver gang noen kaller dette endepunktet
        kandidater.stream()
                .filter(kandidat -> StringUtils.isBlank(kandidat.getAktørId()))
                .forEach(kandidat -> kandidat.setAktørId(kandidatService.hentAktørId(kandidat.getFnr())));
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody Kandidat kandidat) {
        loggBrukAvEndepunkt("opprettKandidat");
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());

        String fnr = kandidatService.hentFnr(kandidat.getAktørId());
        if (!fnr.equals(kandidat.getFnr())) {
            log.warn("Fnr fra frontend og fnr fra aktørregister er forskjellig");
        }
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
        return ResponseEntity.ok(endretKandidat);
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
            return ResponseEntity.notFound().build();
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
