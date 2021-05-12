package no.nav.finnkandidatapi.kafka.samtykke;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.samtykke.SamtykkeService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SamtykkeConsumer {

    private final SamtykkeService samtykkeService;

    public SamtykkeConsumer(SamtykkeService samtykkeService) {
        this.samtykkeService = samtykkeService;
    }

    @KafkaListener(
            topics = "teampam.samtykke-status-1",
            groupId = "finn-kandidat-samtykke-v4",
            clientIdPrefix = "samtykke",
            containerFactory = "aivenKafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        String json = melding.value();
        log.info("Mottatt samtykkemelding: " + json);
        SamtykkeMelding samtykkeMelding = new SamtykkeMelding(json);
        samtykkeService.behandleSamtykke(samtykkeMelding);
    }
}
