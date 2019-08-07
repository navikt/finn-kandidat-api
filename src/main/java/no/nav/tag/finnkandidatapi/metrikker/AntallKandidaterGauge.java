package no.nav.tag.finnkandidatapi.metrikker;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import org.springframework.stereotype.Component;

@Component
public class AntallKandidaterGauge {

    public AntallKandidaterGauge(MeterRegistry meterRegistry, KandidatRepository repository) {
        // TODO: Fjern 10
        meterRegistry.gauge("finn-kandidat.kandidater.antall", repository, repositoriet -> repositoriet.hentKandidater().size() + 10);
    }
}
