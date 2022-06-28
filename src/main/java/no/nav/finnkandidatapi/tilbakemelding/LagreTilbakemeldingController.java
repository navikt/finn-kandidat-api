package no.nav.finnkandidatapi.tilbakemelding;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.RequiredIssuers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_AZUREAD;
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_OPENAM;

@RestController
@RequestMapping
@RequiredIssuers(value = {
        @ProtectedWithClaims(issuer = ISSUER_OPENAM),
        @ProtectedWithClaims(issuer = ISSUER_AZUREAD)
})
public class LagreTilbakemeldingController {

    private final TilbakemeldingRepository repository;

    public LagreTilbakemeldingController(TilbakemeldingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/tilbakemeldinger")
    public ResponseEntity giTilbakemelding(@RequestBody Tilbakemelding tilbakemelding) {
        repository.lagreTilbakemelding(tilbakemelding);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
