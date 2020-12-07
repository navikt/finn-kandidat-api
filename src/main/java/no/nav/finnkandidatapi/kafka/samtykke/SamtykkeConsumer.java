package no.nav.finnkandidatapi.kafka.samtykke;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SamtykkeConsumer {

	@KafkaListener(
			topics = "aapen-pam-samtykke-endret-v1",
			groupId = "finn-kandidat-samtykke",
			clientIdPrefix = "samtykke"
	)
	public void konsumerMelding(ConsumerRecord<String, String> melding) {
		String json = melding.value();
		log.info("JSON samtykke: " + json);
	}
}
