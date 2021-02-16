package no.nav.finnkandidatapi.tilbakemelding;

import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Protected
@RestController
@RequestMapping("/tilbakemeldinger")
public class TilbakemeldingController {

    private final TilbakemeldingRepository repository;
    private final TilgangskontrollService tilgangskontrollService;
    private final TilbakemeldingConfig config;
    private final MetricsClient metricsClient;

    public TilbakemeldingController(
            TilbakemeldingRepository repository,
            TilgangskontrollService tilgangskontrollService,
            TilbakemeldingConfig config,
            MetricsClient metricsClient) {
        this.repository = repository;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
        this.metricsClient = metricsClient;
    }

    @PostMapping
    public ResponseEntity giTilbakemelding(@RequestBody Tilbakemelding tilbakemelding) {
        repository.lagreTilbakemelding(tilbakemelding);

        Event event = new Event("finn-kandidat.tilbakemelding.mottatt")
                .addFieldToReport("behov", tilbakemelding.getBehov())
                .addFieldToReport("tilbakemelding", tilbakemelding.getTilbakemelding());
        metricsClient.report(event);

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
