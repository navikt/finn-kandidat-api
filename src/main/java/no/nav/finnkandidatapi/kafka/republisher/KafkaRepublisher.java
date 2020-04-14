package no.nav.finnkandidatapi.kafka.republisher;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SjekkPermittertUtil;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Component
@Protected
@Slf4j
public class KafkaRepublisher {
    private final HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private final KandidatRepository kandidatRepository;
    private final PermittertArbeidssokerService permittertArbeidssokerService;
    private final VedtakService vedtakService;
    private final TilgangskontrollService tilgangskontrollService;
    private final KafkaRepublisherConfig config;

    @Autowired
    public KafkaRepublisher(
            HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer,
            KandidatRepository kandidatRepository,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService,
            TilgangskontrollService tilgangskontrollService,
            KafkaRepublisherConfig config
    ) {
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.kandidatRepository = kandidatRepository;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    /**
     * Republiser alle kandidater til Kafka. Brukes bare i spesielle tilfeller.
     *
     * @return 200 OK hvis kandidater ble republisert.
     */
    @PostMapping("/internal/kafka/republish" )
    public ResponseEntity republiserAlleKandidater() {
        String ident = sjekkTilgangTilRepublisher();

        List<HarTilretteleggingsbehov> kandidatoppdateringer = kandidatRepository.hentHarTilretteleggingsbehov();

        log.warn("Bruker med ident {} republiserer alle {} kandidater!", ident, kandidatoppdateringer.size());
        kandidatoppdateringer.forEach(oppdatering -> {
            harTilretteleggingsbehovProducer.sendKafkamelding(oppdatering);
        });

        return ResponseEntity.ok().build();
    }

    /*
     * Republiser en enkelt kandidat til Kafka. Brukes bare i spesielle tilfeller.
     */
    @PostMapping("/internal/kafka/republish/{aktørId}" )
    public ResponseEntity republiserKandidat(@PathVariable("aktørId" ) String aktørId) {
        String ident = sjekkTilgangTilRepublisher();

        Optional<HarTilretteleggingsbehov> harTilretteleggingsbehov = kandidatRepository.hentHarTilretteleggingsbehov(aktørId);
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørId);
        Optional<Vedtak> vedtak = vedtakService.hentNyesteVedtakForAktør(aktørId);

        if (harTilretteleggingsbehov.isEmpty() && permittertArbeidssoker.isEmpty() && vedtak.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean harBehov = harTilretteleggingsbehov
                .map(HarTilretteleggingsbehov::getBehov)
                .filter(behov -> !behov.isEmpty())
                .isPresent();

        List<String> behov = new ArrayList<>();
        harTilretteleggingsbehov.ifPresent(tilretteleggingsbehov -> behov.addAll(tilretteleggingsbehov.getBehov()));
        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(permittertArbeidssoker, vedtak);
        if (erPermittert) {
            behov.add(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI);
        }

        HarTilretteleggingsbehov behovMedPermittering = new HarTilretteleggingsbehov(aktørId, harBehov, behov);
        log.warn("Bruker med ident {} republiserer kandidat med aktørId {}.", ident, aktørId);
        harTilretteleggingsbehovProducer.sendKafkamelding(behovMedPermittering);
        return ResponseEntity.ok().build();
    }

    private String sjekkTilgangTilRepublisher() {
        String innloggetNavIdent = tilgangskontrollService.hentInnloggetVeileder().getNavIdent();

        if (!config.getNavIdenterSomKanRepublisere().contains(innloggetNavIdent)) {
            throw new TilgangskontrollException("Bruker med ident " + innloggetNavIdent + " har ikke tilgang til å republisere Kafka-meldinger" );
        }

        return innloggetNavIdent;
    }
}
