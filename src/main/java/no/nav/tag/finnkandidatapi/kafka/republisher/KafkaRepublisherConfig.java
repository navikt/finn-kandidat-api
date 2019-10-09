package no.nav.tag.finnkandidatapi.kafka.republisher;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@Getter
public class KafkaRepublisherConfig {
    private final List<String> navIdenterSomKanRepublisere;

    public KafkaRepublisherConfig(
            @Value("${tilgangskontroll.republisering}") String listeMedNavIdenter
    ) {
        this.navIdenterSomKanRepublisere = Arrays.asList(listeMedNavIdenter.split(","));
    }
}
