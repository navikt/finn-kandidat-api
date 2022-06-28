package no.nav.finnkandidatapi.samtykke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.RequiredIssuers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_AZUREAD;
import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_OPENAM;

@Slf4j
@RequiredIssuers(value = {
        @ProtectedWithClaims(issuer = ISSUER_OPENAM),
        @ProtectedWithClaims(issuer = ISSUER_AZUREAD)
})
@RestController
@RequestMapping("/samtykke")
@RequiredArgsConstructor
public class SamtykkeController {

    private final SamtykkeService samtykkeService;
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/{aktørId}")
    public ResponseEntity<Boolean> harSamtykke(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt("harSamtykke");
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);
        return samtykkeService.harSamtykkeForCV(aktørId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    private void loggBrukAvEndepunkt(String endepunkt) {
        log.info(
                "Bruker med ident {} kaller endepunktet {}.",
                tilgangskontroll.hentInnloggetVeileder().getNavIdent(),
                endepunkt
        );
    }
}
