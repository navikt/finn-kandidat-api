package no.nav.tag.finnkandidatapi.metrikker;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AntallKandidaterMetrikk {

    public AntallKandidaterMetrikk(MeterRegistry meterRegistry, KandidatRepository repository) {
        meterRegistry.gauge("kandidater.antall", repository, repositoriet -> repositoriet.hentKandidater().size());
    }
}
