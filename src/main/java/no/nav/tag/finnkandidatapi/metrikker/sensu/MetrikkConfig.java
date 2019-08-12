package no.nav.tag.finnkandidatapi.metrikker.sensu;

import no.nav.metrics.MetricsClient;
import no.nav.metrics.MetricsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "dev", "prod" })
public class MetrikkConfig {

    @Value("${NAIS_CLUSTER_NAME}")
    private static String miljø;

    static {
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig().withEnvironment(miljø));
    }
}
