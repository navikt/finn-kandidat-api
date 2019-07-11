package no.nav.tag.finnkandidatapi.tilbakemelding;

import no.nav.security.oidc.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Protected
@RestController("/tilbakemelding")
public class TilbakemeldingController {

    private final TilbakemeldingRepository repository;

    public TilbakemeldingController(TilbakemeldingRepository repository) {
        this.repository = repository;
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
        return;
    }
}
