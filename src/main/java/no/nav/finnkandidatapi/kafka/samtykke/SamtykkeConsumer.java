package no.nav.finnkandidatapi.kafka.samtykke;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SamtykkeConsumer {

    @KafkaListener(
            topics = "aapen-pam-samtykke-endret-v1",
            groupId = "finn-kandidat-samtykke-test2",
            clientIdPrefix = "samtykke"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        String json = melding.value();
        log.info("jsonsamtykke: " + json);
    }

}
