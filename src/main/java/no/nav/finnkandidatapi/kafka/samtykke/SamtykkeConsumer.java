package no.nav.finnkandidatapi.kafka.samtykke;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.samtykke.Samtykke;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SamtykkeConsumer implements ConsumerSeekAware {

    @KafkaListener(
            topics = "aapen-pam-samtykke-endret-v1",
            groupId = "finn-kandidat-samtykke-test3",
            clientIdPrefix = "samtykke",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        String json = melding.value();
        Samtykke samtykke = new SamtykkeUtils().deserialiserMelding(json);
    }


    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments,
                                     ConsumerSeekCallback callback) {
        assignments.forEach((tp, offset) -> {
            log.info("Spoler tilbake til begynnelsen for partisjon {}-{}", tp.topic(), tp.partition());
            callback.seekToBeginning(tp.topic(), tp.partition());
        });
    }
}
