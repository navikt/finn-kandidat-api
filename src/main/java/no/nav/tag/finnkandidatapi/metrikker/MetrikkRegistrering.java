package no.nav.tag.finnkandidatapi.metrikker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {

    private final MeterRegistry meterRegistry;

    public MetrikkRegistrering(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        log.info("event=kandidat.opprettet, id={}", event.getKandidat().getId());
        counter("kandidat.opprettet").increment();

        MetricsFactory.createEvent("finn.kandidat.kandidat.opprettet").report();
    }

    private Counter counter(String navn) {
        return Counter.builder("finn-kandidat." + navn)
                .register(meterRegistry);
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        log.info("event=kandidat.endret, id={}", event.getKandidat().getId());
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        log.info("event=kandidat.slettet, id={}", event.getId());
    }
}
