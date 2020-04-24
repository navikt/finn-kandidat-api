package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermitteringHandler {
    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    public PermitteringHandler(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
    }

    @EventListener
    public void permitteringEndretEllerOpprettet(PermittertArbeidssokerEndretEllerOpprettet event) {
        harTilretteleggingsbehovProducer.sendKafkamelding(
                sammenstillBehov.lagbehov(event.getPermittertArbeidssoker())
        );
    }
}
