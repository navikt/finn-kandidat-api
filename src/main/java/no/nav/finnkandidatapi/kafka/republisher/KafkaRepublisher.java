package no.nav.finnkandidatapi.kafka.republisher;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_ISSO;

@RestController
@Component
@ProtectedWithClaims(issuer = ISSUER_ISSO)
@Slf4j
public class KafkaRepublisher {
    private final HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private final KandidatRepository kandidatRepository;
    private final RepublisherRepository republisherRepository;
    private final SammenstillBehov sammenstillBehov;
    private final TilgangskontrollService tilgangskontrollService;
    private final KafkaRepublisherConfig config;

    @Autowired
    public KafkaRepublisher(
            HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer,
            KandidatRepository kandidatRepository,
            RepublisherRepository republisherRepository,
            TilgangskontrollService tilgangskontrollService,
            SammenstillBehov sammenstillBehov,
            KafkaRepublisherConfig config
    ) {
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.kandidatRepository = kandidatRepository;
        this.republisherRepository = republisherRepository;
        this.sammenstillBehov = sammenstillBehov;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    /**
     * Republiser alle kandidater som har tilretteleggingsbehov til Kafka. Brukes bare i spesielle tilfeller.
     *
     * @return 200 OK hvis kandidater ble republisert.
     */
    @PostMapping("/internal/kafka/republish/tilrettelegging")
    public ResponseEntity republiserAlleTilretteleggingsbehov() {
        String ident = sjekkTilgangTilRepublisher();

        List<HarTilretteleggingsbehov> kandidatoppdateringer = kandidatRepository.hentHarTilretteleggingsbehov();

        log.warn("Bruker med ident {} republiserer alle {} kandidater!", ident, kandidatoppdateringer.size());
        kandidatoppdateringer.forEach(oppdatering -> {
            harTilretteleggingsbehovProducer.sendKafkamelding(
                    sammenstillBehov.lagbehovKandidat(oppdatering)
            );

        });

        return ResponseEntity.ok().build();
    }

    /**
     * Republiser alle kandidater til Kafka. Brukes bare i spesielle tilfeller.
     *
     * @return 200 OK hvis kandidater ble republisert.
     */
    @PostMapping("/internal/kafka/republish")
    public ResponseEntity republiser() {
        String ident = sjekkTilgangTilRepublisher();

        var aktørider = republisherRepository.hentAktørider();

        log.warn("Bruker med ident {} republiserer alle {} kandidatdata", ident, aktørider.size());
        var filtrerteTilretteleggingsbehov = aktørider.stream().map(aktørId ->
                sammenstillBehov.lagbehov(aktørId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
        ).filter(harTilretteleggingsbehov ->
                harTilretteleggingsbehov.isHarTilretteleggingsbehov() || !harTilretteleggingsbehov.getBehov().isEmpty()
        ).collect(Collectors.toList());

        log.info("Antall filtrerte tilretteleggingsbehov " + filtrerteTilretteleggingsbehov.size());

        //filtrerteTilretteleggingsbehov.forEach(harTilretteleggingsbehovProducer::sendKafkamelding);

        return ResponseEntity.ok().build();
    }

    /*
     * Republiser en enkelt kandidat til Kafka. Brukes bare i spesielle tilfeller.
     */
    @PostMapping("/internal/kafka/republish/{aktørId}")
    public ResponseEntity republiserKandidat(@PathVariable("aktørId") String aktørId) {
        String ident = sjekkTilgangTilRepublisher();

        HarTilretteleggingsbehov harTilretteleggingsbehov = sammenstillBehov.lagbehov(aktørId);

        if (harTilretteleggingsbehov == null || harTilretteleggingsbehov.getBehov().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        log.warn("Bruker med ident {} republiserer kandidat med aktørId {}.", ident, aktørId);
        harTilretteleggingsbehovProducer.sendKafkamelding(harTilretteleggingsbehov);
        return ResponseEntity.ok().build();
    }

    private String sjekkTilgangTilRepublisher() {
        String innloggetNavIdent = tilgangskontrollService.hentInnloggetVeileder().getNavIdent();

        if (!config.getNavIdenterSomKanRepublisere().contains(innloggetNavIdent)) {
            throw new TilgangskontrollException("Bruker med ident " + innloggetNavIdent + " har ikke tilgang til å republisere Kafka-meldinger");
        }

        return innloggetNavIdent;
    }
}
