package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakEndret;
import no.nav.finnkandidatapi.vedtak.VedtakOpprettet;
import no.nav.finnkandidatapi.vedtak.VedtakSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VedtakHandler {

    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer;

    public VedtakHandler(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer, AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.aivenHarTilretteleggingsbehovProducer = aivenHarTilretteleggingsbehovProducer;
    }

    @EventListener
    public void vedtakOpprettet(VedtakOpprettet vedtakOpprettet) {
        mottattVedtakEvent(vedtakOpprettet.getVedtak());
    }

    @EventListener
    public void vedtakEndret(VedtakEndret vedtakEndret) {
        mottattVedtakEvent(vedtakEndret.getVedtak());
    }

    @EventListener
    public void vedtakSlettet(VedtakSlettet vedtakSlettet) {
        mottattVedtakEvent(vedtakSlettet.getVedtak());
    }

    private void mottattVedtakEvent(Vedtak vedtak) {
        HarTilretteleggingsbehov behov = sammenstillBehov.lagbehov(vedtak);
        harTilretteleggingsbehovProducer.sendKafkamelding(behov);
        aivenHarTilretteleggingsbehovProducer.sendKafkamelding(behov);
    }
}
