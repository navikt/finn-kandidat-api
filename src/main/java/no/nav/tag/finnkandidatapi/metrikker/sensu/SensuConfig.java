package no.nav.tag.finnkandidatapi.metrikker.sensu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensuConfig {

    // TODO flytt til config
    private static final String SENSU_HOSTNAME = "sensu.nais";
    private static final int SENSU_PORT = 3030;

    // TODO flytt til config
    @Value("${NAIS_CLUSTER_NAME:lokalt}")
    private String environment;

    @Bean
    public SensuClient sensuClient() {
        return new SensuClient(environment, SENSU_PORT, SENSU_HOSTNAME);
    }
}
