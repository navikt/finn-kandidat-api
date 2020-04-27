package no.nav.finnkandidatapi.kafka.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MidlertidigUtilgjengeligScheduler {

    private final static String HVER_NATT_KLOKKEN_ETT = "0 0 * * * *";
    private final static String HVER_TIME = "0 0 * * * *";
    private final MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;
    private final HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private final SammenstillBehov sammenstillBehov;

    public MidlertidigUtilgjengeligScheduler(MidlertidigUtilgjengeligService midlertidigUtilgjengeligService, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer, SammenstillBehov sammenstillBehov) {
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.sammenstillBehov = sammenstillBehov;
    }

    /**
     * Republiser alle midlertidig tilgjengelige hver natt, for å oppdatere med riktig filter i søket.
     */
    @Scheduled(cron = HVER_TIME)
    @SchedulerLock(name = "oppdaterMidlertidigUtilgjengelig")
    public void oppdaterMidlertidigUtilgjengelig() {
        LockAssert.assertLocked();
        List<MidlertidigUtilgjengelig> alleMidlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentAlleMidlertidigUtilgjengelig();
        log.info("Scheduler med bruker {} republiserer alle {} midlertidig utilgjengelig!", Brukertype.SYSTEM, alleMidlertidigUtilgjengelig.size());
        alleMidlertidigUtilgjengelig.forEach(
                midlertidigUtilgjengelig -> harTilretteleggingsbehovProducer.sendKafkamelding(
                        sammenstillBehov.lagbehov(midlertidigUtilgjengelig)
                )
        );
    }
}
