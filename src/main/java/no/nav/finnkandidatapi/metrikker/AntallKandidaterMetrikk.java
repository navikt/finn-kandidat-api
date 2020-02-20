package no.nav.finnkandidatapi.metrikker;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AntallKandidaterMetrikk {

    public AntallKandidaterMetrikk(MeterRegistry meterRegistry, KandidatRepository repository) {
        meterRegistry.gauge("kandidater.antall", repository, repositoriet -> repositoriet.hentKandidater().size());
    }
}
