package no.nav.finnkandidatapi.kafka.vedtakEndret;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vedtak-endret")
public class VedtakEndretConfig {
    private String topic;
}