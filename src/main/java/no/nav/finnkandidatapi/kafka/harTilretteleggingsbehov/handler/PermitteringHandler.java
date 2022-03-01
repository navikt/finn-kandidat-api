package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermitteringHandler {
    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;
    private AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer;

    public PermitteringHandler(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer, AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
        this.aivenHarTilretteleggingsbehovProducer = aivenHarTilretteleggingsbehovProducer;
    }

    @EventListener
    public void permitteringEndretEllerOpprettet(PermittertArbeidssokerEndretEllerOpprettet event) {
        HarTilretteleggingsbehov behov = sammenstillBehov.lagbehov(event.getPermittertArbeidssoker());
        harTilretteleggingsbehovProducer.sendKafkamelding(behov);
        aivenHarTilretteleggingsbehovProducer.sendKafkamelding(behov);
    }

    @EventListener
    public void permitteringSlettet(PermittertArbeidssokerSlettet event) {
        HarTilretteleggingsbehov behov = sammenstillBehov.lagbehov(event.getAktørId());
        //harTilretteleggingsbehovProducer.sendKafkamelding(behov); // Arbeidsplassen har aldri fått denne
        aivenHarTilretteleggingsbehovProducer.sendKafkamelding(behov);
    }
}
