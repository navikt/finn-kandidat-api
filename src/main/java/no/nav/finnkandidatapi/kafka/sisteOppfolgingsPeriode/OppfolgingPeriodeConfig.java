package no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "siste-oppfolgingsperiode")
public class OppfolgingPeriodeConfig {
    private String topic;
}
