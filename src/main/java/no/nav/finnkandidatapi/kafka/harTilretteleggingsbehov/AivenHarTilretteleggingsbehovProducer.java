package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;


@Component
@Slf4j
public class AivenHarTilretteleggingsbehovProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public AivenHarTilretteleggingsbehovProducer(
            @Qualifier("aivenKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            @Value("${tillretteleggingsbehov.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendKafkamelding(HarTilretteleggingsbehov melding) {
        String payload;
        try {
            payload = new ObjectMapper().writeValueAsString(melding);
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere HarTilretteleggingsbehov", e);
            return;
        }
        send(melding.getAktoerId(), payload);
    }

    private void send(String key, String payload) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);
        future.addCallback(result -> {
                    log.info(
                            "Kandidats behov for tilrettelegging sendt på Kafka-topic, aktørId: {}, offset: {}",
                            key,
                            result.getRecordMetadata().offset()
                    );
                },
                exception -> {
                    log.error("Kunne ikke sende kandidat på Kafka-topic, aktørId: {}", key, exception);
                });
    }
}
