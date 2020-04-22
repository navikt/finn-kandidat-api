package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MidlertidigUtilgjengeligHandler {

    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    public MidlertidigUtilgjengeligHandler(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
    }

    @EventListener
    public void midlertidigUtilgjengeligOpprettet(MidlertidigUtilgjengeligOpprettet midlertidigUtilgjengeligOpprettet) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligOpprettet.getMidlertidigUtilgjengelig());
    }

    @EventListener
    public void midlertidigUtilgjengeligEndret(MidlertidigUtilgjengeligEndret midlertidigUtilgjengeligEndret) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligEndret.getMidlertidigUtilgjengelig());
    }

    @EventListener
    public void midlertidigUtilgjengeligSlettet(MidlertidigUtilgjengeligSlettet midlertidigUtilgjengeligSlettet) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligSlettet.getMidlertidigUtilgjengelig());
    }

    private void mottattMidlertidigUtilgjengeligEvent(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {
        harTilretteleggingsbehovProducer.sendKafkamelding(
                sammenstillBehov.lagbehov(midlertidigUtilgjengelig)
        );
    }
}
