package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
            KandidatEndret kandidatEndret = new KandidatEndret(aktørId, harTilretteleggingsbehov);
            ObjectMapper mapper = new ObjectMapper();
            String serialisertKandidatEndret = mapper.writeValueAsString(kandidatEndret);

            kafkaTemplate.send(
                    topic,
                    aktørId,
                    serialisertKandidatEndret
            ).get();

            log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic");

        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere kandidat endret", e);
        } catch (InterruptedException | ExecutionException e) {
            // TOOD: Håndter kafka-meldinger som ikke ble sendt.
            log.error("Kunne ikke sende kandidat på Kafka-topic", e);
        }
    }
}
