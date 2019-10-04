package no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oppfolging-avsluttet")
public class ConsumerProps {
    private String topic;
}
