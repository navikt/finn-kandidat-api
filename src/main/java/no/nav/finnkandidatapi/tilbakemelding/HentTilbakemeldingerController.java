package no.nav.finnkandidatapi.tilbakemelding;

import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_ISSO;

@RestController
@RequestMapping
@ProtectedWithClaims(issuer = ISSUER_ISSO)
public class HentTilbakemeldingerController {

    private final TilbakemeldingRepository repository;
    private final TilgangskontrollService tilgangskontrollService;
    private final TilbakemeldingConfig config;

    public HentTilbakemeldingerController(
            TilbakemeldingRepository repository,
            TilgangskontrollService tilgangskontrollService,
            TilbakemeldingConfig config
    ) {
        this.repository = repository;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    @GetMapping("/tilbakemeldinger")
    public List<Tilbakemelding> hentAlleTilbakemeldinger() {
        sjekkLesetilgangTilTilbakemeldinger();
        return repository.hentAlleTilbakemeldinger();
    }

    private void sjekkLesetilgangTilTilbakemeldinger() {
        String innloggetNavIdent = tilgangskontrollService.hentInnloggetVeileder().getNavIdent();
        if (!config.getNavIdenterSomHarLesetilgangTilTilbakemeldinger().contains(innloggetNavIdent)) {
            throw new TilgangskontrollException("Bruker med ident " + innloggetNavIdent + " har ikke tilgang til å se tilbakemeldinger");
        }
    }
}
