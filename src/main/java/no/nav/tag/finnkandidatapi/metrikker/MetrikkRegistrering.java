package no.nav.tag.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        log.info("event=kandidat.opprettet, id={}", event.getKandidat().getId());
        MetricsFactory.createEvent("finn-kandidat.kandidat.opprettet").report();
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        log.info("event=kandidat.endret, id={}", event.getKandidat().getId());
        MetricsFactory.createEvent("finn-kandidat.kandidat.endret").report();
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        log.info("event=kandidat.slettet, id={}", event.getId());
        MetricsFactory.createEvent("finn-kandidat.kandidat.slettet").report();
    }
}
