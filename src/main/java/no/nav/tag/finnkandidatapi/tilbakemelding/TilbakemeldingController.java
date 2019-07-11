package no.nav.tag.finnkandidatapi.tilbakemelding;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Protected
@RestController
@RequestMapping("/tilbakemelding")
public class TilbakemeldingController {

    private final TilbakemeldingRepository repository;
    private final TilgangskontrollService tilgangskontrollService;
    private final TilbakemeldingConfig config;

    public TilbakemeldingController(
            TilbakemeldingRepository repository,
            TilgangskontrollService tilgangskontrollService,
            TilbakemeldingConfig config) {
        this.repository = repository;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    @PostMapping
    public ResponseEntity giTilbakemelding(Tilbakemelding tilbakemelding) {
        repository.lagreTilbakemelding(tilbakemelding);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
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
