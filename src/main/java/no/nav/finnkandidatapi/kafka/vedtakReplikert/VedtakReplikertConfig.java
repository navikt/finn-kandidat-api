package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vedtak-replikert")
public class VedtakReplikertConfig {
    private String topic;
}