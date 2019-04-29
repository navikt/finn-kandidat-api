package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController {

    private final KandidatService kandidatService;
    private final TokenUtils tokenUtils;

    @GetMapping("/{fnr}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("fnr") String fnr) {
        Kandidat kandidat = kandidatService.hentNyesteKandidat(fnr).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(@RequestBody Kandidat kandidat) {
        Veileder veileder = tokenUtils.hentInnloggetVeileder();
        Kandidat lagretKandidat = kandidatService.lagreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }
}
