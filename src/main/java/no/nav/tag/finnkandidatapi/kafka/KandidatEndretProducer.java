package no.nav.tag.finnkandidatapi.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class  KandidatEndretProducer {
    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;

    public KandidatEndretProducer(KafkaTemplate<String, String> kafkaTemplate,
                                  @Value("${kanidat-endret.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void kandidatEndret(String aktørId, Boolean harTilretteleggingsbehov) {
        try {
            kafkaTemplate.send(
                    topic,
                    aktørId,
                    harTilretteleggingsbehov.toString()
            ).get();

            log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kunne ikke sende kandidat på Kafka-topic", e);
        }
    }
}