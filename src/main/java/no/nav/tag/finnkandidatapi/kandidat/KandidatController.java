package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
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

    @GetMapping("/fnr/{fnr}")
    public ResponseEntity<Kandidat> hentKandidatMedFnr(@PathVariable("fnr") String fnr) {
        loggBrukAvEndepunkt("hentKandidat");
        String aktørId = kandidatService.hentAktørId(fnr);
        return hentKandidat(aktørId);
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("hentKandidat");
        tilgangskontroll.sjekkPilotTilgang();
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);
        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktørId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping("/{fnr}/aktorId")
    public ResponseEntity<String> hentAktørId(@PathVariable("fnr") String fnr) {
        loggBrukAvEndepunkt("hentAktørId");
        tilgangskontroll.sjekkPilotTilgang();

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
        tilgangskontroll.sjekkPilotTilgang();
        String fnr = kandidatService.hentFnr(aktørId);
        return ResponseEntity.ok(fnr);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        loggBrukAvEndepunkt("hentKandidater");
        tilgangskontroll.sjekkPilotTilgang();
        List<Kandidat> kandidater =
                kandidatService.hentKandidater().stream()
                .filter(kandidat -> tilgangskontroll.harLesetilgangTilKandidat(kandidat.getAktørId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody KandidatDto kandidat) {
        loggBrukAvEndepunkt("opprettKandidat");
        tilgangskontroll.sjekkPilotTilgang();
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());

        boolean kandidatEksisterer = kandidatService.kandidatEksisterer(kandidat.getAktørId());
        if (kandidatEksisterer) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        String fnr = kandidatService.hentFnr(kandidat.getAktørId());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat opprettetKandidat = kandidatService.opprettKandidat(fnr, kandidat, veileder)
                .orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(opprettetKandidat);
    }

    @PutMapping
    public ResponseEntity<Kandidat> endreKandidat(@RequestBody KandidatDto kandidatDto) {
        loggBrukAvEndepunkt("endreKandidat");
        tilgangskontroll.sjekkPilotTilgang();
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidatDto.getAktørId());
        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat endretKandidat = kandidatService.endreKandidat(kandidatDto, veileder)
                .orElseThrow(FinnKandidatException::new);
        return ResponseEntity.ok(endretKandidat);
    }

    @GetMapping("/{aktørId}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("hentSkrivetilgang");
        tilgangskontroll.sjekkPilotTilgang();
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(aktørId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{aktørId}")
    public ResponseEntity slettKandidat(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("slettKandidat");
        tilgangskontroll.sjekkPilotTilgang();
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
