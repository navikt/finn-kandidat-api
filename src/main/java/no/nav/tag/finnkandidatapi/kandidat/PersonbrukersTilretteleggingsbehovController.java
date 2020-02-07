package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ProtectedWithClaims(issuer = "selvbetjening")
@RestController
public class PersonbrukersTilretteleggingsbehovController {

    private final KandidatService kandidatService;
    private final TokenUtils tokenUtils;

    public PersonbrukersTilretteleggingsbehovController(KandidatService kandidatService, TokenUtils tokenUtils) {
        this.kandidatService = kandidatService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("/tilretteleggingsbehov")
    public ResponseEntity<Kandidat> hentTilretteleggingsbehov() {
        String fnr = tokenUtils.hentInnloggetBruker();
        String aktørId = kandidatService.hentAktørId(fnr);
        Kandidat kandidat = kandidatService.hentNyesteKandidat(aktørId).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }
}
