package no.nav.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.RequiredIssuers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static no.bekk.bekkopen.person.FodselsnummerValidator.isValid;
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_AZUREAD;

@Slf4j
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = ISSUER_AZUREAD)
public class KandidatController {

    private final KandidatService kandidatService;
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/{fnrEllerAktørId}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("fnrEllerAktørId") String fnrEllerAktørId) {
        loggBrukAvEndepunkt("hentKandidat");

        boolean erFnr = isValid(fnrEllerAktørId);
        String aktørId = erFnr ? kandidatService.hentAktørId(fnrEllerAktørId) : fnrEllerAktørId;

        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);

        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktørId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @PostMapping
    public ResponseEntity<Kandidat> opprettKandidat(@RequestBody KandidatDto kandidat) {
        loggBrukAvEndepunkt("opprettKandidat");

        if (kandidat.getAktørId() == null && kandidat.getFnr() != null) {
            String aktørId = kandidatService.hentAktørId(kandidat.getFnr());
            kandidat.setAktørId(aktørId);
        } else if (kandidat.getFnr() == null && kandidat.getAktørId() != null) {
            String fnr = kandidatService.hentFnr(kandidat.getAktørId());
            kandidat.setFnr(fnr);
        } else if (kandidat.getFnr() == null && kandidat.getAktørId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getAktørId());

        boolean kandidatEksisterer = kandidatService.kandidatEksisterer(kandidat.getAktørId());
        if (kandidatEksisterer) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat opprettetKandidat = kandidatService.opprettKandidat(kandidat, veileder)
                .orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(opprettetKandidat);
    }

    @PutMapping
    public ResponseEntity<Kandidat> endreKandidat(@RequestBody KandidatDto kandidatDto) {
        loggBrukAvEndepunkt("endreKandidat");

        if (kandidatDto.getAktørId() == null && kandidatDto.getFnr() != null) {
            String aktørId = kandidatService.hentAktørId(kandidatDto.getFnr());
            kandidatDto.setAktørId(aktørId);
        } else if (kandidatDto.getAktørId() == null && kandidatDto.getFnr() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidatDto.getAktørId());

        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat endretKandidat = kandidatService.endreKandidat(kandidatDto, veileder)
                .orElseThrow(FinnKandidatException::new);
        return ResponseEntity.ok(endretKandidat);
    }

    @DeleteMapping("/{fnrEllerAktørId}")
    public ResponseEntity slettKandidat(@PathVariable("fnrEllerAktørId") String fnrEllerAktørId) {
        loggBrukAvEndepunkt("slettKandidat");

        boolean erFnr = isValid(fnrEllerAktørId);
        String aktørId = erFnr ? kandidatService.hentAktørId(fnrEllerAktørId) : fnrEllerAktørId;

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
