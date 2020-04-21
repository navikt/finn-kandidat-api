package no.nav.finnkandidatapi.kafka.republisher;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SjekkPermittertUtil;
import no.nav.finnkandidatapi.kafka.midlertidigutilgjengelig.MidlertidigTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@RestController
@Component
@Protected
@Slf4j
public class KafkaRepublisher {
    private final HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private final KandidatRepository kandidatRepository;
    private final PermittertArbeidssokerService permittertArbeidssokerService;
    private final VedtakService vedtakService;
    private final MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;
    private final TilgangskontrollService tilgangskontrollService;
    private final KafkaRepublisherConfig config;

    @Autowired
    public KafkaRepublisher(
            HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer,
            KandidatRepository kandidatRepository,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService,
            MidlertidigUtilgjengeligService midlertidigUtilgjengeligService, TilgangskontrollService tilgangskontrollService,
            KafkaRepublisherConfig config
    ) {
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.kandidatRepository = kandidatRepository;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    /**
     * Republiser alle kandidater til Kafka. Brukes bare i spesielle tilfeller.
     *
     * @return 200 OK hvis kandidater ble republisert.
     */
    @PostMapping("/internal/kafka/republish/tilrettelegging")
    //TODO: Denne må oppdateres med blant annet permitteringer før den kjøres.
    public ResponseEntity republiserAlleTilretteleggingsbehov() {
        String ident = sjekkTilgangTilRepublisher();

        List<HarTilretteleggingsbehov> kandidatoppdateringer = kandidatRepository.hentHarTilretteleggingsbehov();

        log.warn("Bruker med ident {} republiserer alle {} kandidater!", ident, kandidatoppdateringer.size());
        kandidatoppdateringer.forEach(oppdatering -> {
            List<String> behovFilter = lagbehov(oppdatering.getAktoerId(), Optional.of(oppdatering));
            HarTilretteleggingsbehov behov = new HarTilretteleggingsbehov(oppdatering.getAktoerId(), CollectionUtils.isNotEmpty(behovFilter), behovFilter);
            harTilretteleggingsbehovProducer.sendKafkamelding(behov);
        });

        return ResponseEntity.ok().build();
    }

    /*
     * Republiser en enkelt kandidat til Kafka. Brukes bare i spesielle tilfeller.
     */
    @PostMapping("/internal/kafka/republish/{aktørId}")
    public ResponseEntity republiserKandidat(@PathVariable("aktørId") String aktørId) {
        String ident = sjekkTilgangTilRepublisher();

        List<String> behov = lagbehov(aktørId, Optional.empty());

        if (behov.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HarTilretteleggingsbehov behovMedPermittering = new HarTilretteleggingsbehov(aktørId, CollectionUtils.isNotEmpty(behov), behov);
        log.warn("Bruker med ident {} republiserer kandidat med aktørId {}.", ident, aktørId);
        harTilretteleggingsbehovProducer.sendKafkamelding(behovMedPermittering);
        return ResponseEntity.ok().build();
    }

    public List<String> lagbehov(String aktørId, Optional<HarTilretteleggingsbehov> tilretteleggingsbehov) {
        Optional<HarTilretteleggingsbehov> harTilretteleggingsbehov = tilretteleggingsbehov.or(() -> kandidatRepository.hentHarTilretteleggingsbehov(aktørId));
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørId);
        Optional<Vedtak> vedtak = vedtakService.hentNyesteVedtakForAktør(aktørId);
        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(aktørId);

        List<String> tilretteleggingsbehovFilter = harTilretteleggingsbehov.map(HarTilretteleggingsbehov::getBehov).orElse(emptyList());

        Optional<String> permitteringFilter = SjekkPermittertUtil.sjekkOmErPermittert(permittertArbeidssoker, vedtak)
                ? Optional.of(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI)
                : Optional.empty();

        Optional<String> midlertidigUtilgjengeligFilter = MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter(midlertidigUtilgjengelig);

        return concatToList(
                tilretteleggingsbehovFilter.stream(),
                permitteringFilter.stream(),
                midlertidigUtilgjengeligFilter.stream()
        );
    }

    private <T> List<T> concatToList(Stream<T>... s) {
        return Arrays.stream(s).reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }

    private String sjekkTilgangTilRepublisher() {
        String innloggetNavIdent = tilgangskontrollService.hentInnloggetVeileder().getNavIdent();

        if (!config.getNavIdenterSomKanRepublisere().contains(innloggetNavIdent)) {
            throw new TilgangskontrollException("Bruker med ident " + innloggetNavIdent + " har ikke tilgang til å republisere Kafka-meldinger");
        }

        return innloggetNavIdent;
    }
}
