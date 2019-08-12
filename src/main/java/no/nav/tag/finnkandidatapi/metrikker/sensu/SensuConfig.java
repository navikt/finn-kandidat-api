package no.nav.tag.finnkandidatapi.metrikker.sensu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensuConfig {

    @Value("${sensu.hostname}")
    private String hostname;

    @Value("${sensu.port}")
    private int port;

    @Value("${NAIS_CLUSTER_NAME:lokalt}")
    private String environment;

    @Bean
    public SensuClient sensuClient() {
        return new SensuClient(environment, hostname, port, "finn-kandidat-api");
    }
}
