package no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oppfolging-endret")
public class OppfolgingEndretConfig {
    private String topic;
}