package no.nav.finnkandidatapi.kafka.republisher;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Component
@Protected
@Slf4j
public class KafkaRepublisher {
    private final HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private final KandidatRepository kandidatRepository;
    private final MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;
    private final SammenstillBehov sammenstillBehov;
    private final TilgangskontrollService tilgangskontrollService;
    private final KafkaRepublisherConfig config;

    private final static String HVER_NATT_KLOKKEN_ETT = " 0 * * * * *";

    @Autowired
    public KafkaRepublisher(
            HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer,
            KandidatRepository kandidatRepository,
            MidlertidigUtilgjengeligService midlertidigUtilgjengeligService,
            TilgangskontrollService tilgangskontrollService,
            SammenstillBehov sammenstillBehov,
            KafkaRepublisherConfig config
    ) {
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.kandidatRepository = kandidatRepository;
        this.sammenstillBehov = sammenstillBehov;
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.config = config;
    }

    /**
     * Republiser alle midlertidig tilgjengelige hver natt, for å oppdatere med riktig filter i søket.
     */
    @Scheduled(cron=HVER_NATT_KLOKKEN_ETT)
    @SchedulerLock(name = "oppdaterMidlertidigUtilgjengelig")
    public void oppdaterMidlertidigUtilgjengelig() {
        LockAssert.assertLocked();
        List<MidlertidigUtilgjengelig> alleMidlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentAlleMidlertidigUtilgjengelig();
        log.warn("Scheduler med bruker {} republiserer alle {} midlertidig utilgjengelig!", Brukertype.SYSTEM, alleMidlertidigUtilgjengelig.size());
        alleMidlertidigUtilgjengelig.forEach(
                midlertidigUtilgjengelig -> harTilretteleggingsbehovProducer.sendKafkamelding(
                        sammenstillBehov.lagbehov(midlertidigUtilgjengelig)
                )
        );
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
