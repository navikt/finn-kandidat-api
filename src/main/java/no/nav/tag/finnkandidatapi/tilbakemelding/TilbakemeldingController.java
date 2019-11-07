package no.nav.tag.finnkandidatapi.tilbakemelding;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.metrikker.TilbakemeldingMottatt;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Protected
@RestController
@RequestMapping("/tilbakemeldinger")
public class TilbakemeldingController {

    private final TilbakemeldingRepository repository;
    private final TilgangskontrollService tilgangskontrollService;
    private final TilbakemeldingConfig config;
    private final ApplicationEventPublisher eventPublisher;

    public TilbakemeldingController(
            TilbakemeldingRepository repository,
            TilgangskontrollService tilgangskontrollService,
            TilbakemeldingConfig config,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity giTilbakemelding(@RequestBody Tilbakemelding tilbakemelding) {
        repository.lagreTilbakemelding(tilbakemelding);
        eventPublisher.publishEvent(new TilbakemeldingMottatt(tilbakemelding));
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
