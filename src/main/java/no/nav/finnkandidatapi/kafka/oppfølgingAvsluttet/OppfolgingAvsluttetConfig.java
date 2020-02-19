package no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oppfolging-avsluttet")
public class OppfolgingAvsluttetConfig {
    private String topic;
}
