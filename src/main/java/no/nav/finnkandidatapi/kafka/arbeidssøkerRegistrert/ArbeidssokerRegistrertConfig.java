package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "arbeidssoker-registrert")
public class ArbeidssokerRegistrertConfig {
    private String topic;
}