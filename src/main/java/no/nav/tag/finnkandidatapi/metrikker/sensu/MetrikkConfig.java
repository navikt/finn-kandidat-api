package no.nav.tag.finnkandidatapi.metrikker.sensu;

import no.nav.metrics.MetricsClient;
import no.nav.metrics.MetricsConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "dev", "prod" })
public class MetrikkConfig {

    static {
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig());
    }
}
