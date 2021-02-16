package no.nav.finnkandidatapi.metrikker.sensu;

import no.nav.common.metrics.Event;
import no.nav.common.metrics.InfluxClient;
import no.nav.common.metrics.MetricsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
public class MetrikkConfig {

    // TODO: Skal denne være profilstyrt?
    @Bean
    @Primary
    @Profile({ "dev", "prod" })
    public MetricsClient metricsClient() {
        return new InfluxClient();
    }

    @Bean
    @Profile("local")
    public static MetricsClient metricsClientFake() {
        return new MetricsClient() {
            @Override
            public void report(Event event) {
                // Ikke gjør noe
            }

            @Override
            public void report(String eventName, Map<String, Object> fields, Map<String, String> tags, long timestampInMilliseconds) {
                // Ikke gjør noe
            }
        };
    }
}
