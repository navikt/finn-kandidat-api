package no.nav.tag.finnkandidatapi.kafka.republisher;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.kafka.KandidatEndret;
import no.nav.tag.finnkandidatapi.kafka.KandidatEndretProducer;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
@Protected
@Slf4j
public class KafkaRepublisher {
    private final KandidatEndretProducer kandidatEndretProducer;
    private final KandidatRepository kandidatRepository;
    private final TilgangskontrollService tilgangskontrollService;
    private final KafkaRepublisherConfig config;

    @Autowired
    public KafkaRepublisher(
            KandidatEndretProducer kandidatEndretProducer, KandidatRepository kandidatRepository, TilgangskontrollService tilgangskontrollService, KafkaRepublisherConfig config) {
        this.kandidatEndretProducer = kandidatEndretProducer;
        this.kandidatRepository = kandidatRepository;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    /**
     * Republiser alle kandidater til Kafka. Brukes bare i spesielle tilfeller.
     * @return 200 OK hvis kandidater ble republisert.
     */
    @PostMapping("/internal/kafka/republish")
    public ResponseEntity republiserAlleKandidater() {
        sjekkTilgangTilRepublisher();

        for (KandidatEndret endretKandidat : kandidatRepository.hentSisteKandidatendringer()) {
            kandidatEndretProducer.kandidatEndret(endretKandidat.getAktoerId(), endretKandidat.isHarTilretteleggingsbehov());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private void sjekkTilgangTilRepublisher() {
        String innloggetNavIdent = tilgangskontrollService.hentInnloggetVeileder().getNavIdent();

        if (!config.getNavIdenterSomKanRepublisere().contains(innloggetNavIdent)) {
            throw new TilgangskontrollException("Bruker med ident " + innloggetNavIdent + " har ikke tilgang til å republisere Kafka-meldinger");
        }
    }
}
