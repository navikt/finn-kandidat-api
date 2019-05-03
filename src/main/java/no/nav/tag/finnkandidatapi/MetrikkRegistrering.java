package no.nav.tag.finnkandidatapi;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
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
    public void kandidatOpprettet(Kandidat kandidat) {
        log.info("Kandidat opprettet, id={}", kandidat.getId());
        counter("kandidat.opprettet").increment();
    }

    private Counter counter(String navn) {
        return Counter.builder("finn-kandidat." + navn)
                .register(meterRegistry);
    }
}
