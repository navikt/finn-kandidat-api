package no.nav.tag.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.metrikker.sensu.SensuClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {

    private SensuClient sensuClient;

    public MetrikkRegistrering(SensuClient sensuClient) {
        this.sensuClient = sensuClient;
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        log.info("event=kandidat.opprettet, id={}", event.getKandidat().getId());
        sensuClient.sendEvent("kandidat.opprettet");
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        log.info("event=kandidat.endret, id={}", event.getKandidat().getId());
        sensuClient.sendEvent("kandidat.endret");
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        log.info("event=kandidat.slettet, id={}", event.getId());
        sensuClient.sendEvent("kandidat.slettet");
    }
}
