package no.nav.tag.finnkandidatapi.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class KandidatEndretProducer {

    private static final String TOPIC = "aapen-tag-kandidatEndret-v1-default";
    private KafkaTemplate<String, Boolean> kafkaTemplate;

    public KandidatEndretProducer(KafkaTemplate<String, Boolean> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void kandidatEndret(String aktørId, Boolean harTilretteleggingsbehov) {

        try {
            kafkaTemplate.send(
                    TOPIC,
                    aktørId,
                    harTilretteleggingsbehov
            ).get();

            log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kunne ikke sende kandidat på Kafka-topic", e);
        }
    }
}
