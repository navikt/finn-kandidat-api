package no.nav.finnkandidatapi.kandidat;

import no.nav.finnkandidatapi.tilgangskontroll.TokenUtils;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_TOKENX;

@ProtectedWithClaims(issuer = ISSUER_TOKENX)
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
        String fnr = tokenUtils.hentInnloggetBrukersFødselsnummer();
        String aktørId = kandidatService.hentAktørId(fnr);
        Optional<Kandidat> kandidat = kandidatService.hentNyesteKandidat(aktørId);
        return kandidat
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
