package no.nav.tag.finnkandidatapi.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetUtils.deserialiserMelding;

@Slf4j
@Component
@AllArgsConstructor
public class OppfølgingAvsluttetConsumer {

    private KandidatService kandidatService;
    private ConsumerProps consumerProps;

    @KafkaListener(topics = "#{consumerProps.getTopic()}", groupId = "finn-kandidat")
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer avsluttet oppfølging melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = deserialiserMelding(melding.value());
        kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);
    }
}
