package no.nav.tag.finnkandidatapi.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetUtils.deserialiserMelding;

@Slf4j
@Component
@EnableKafka
@Profile({"kafka-test", "dev", "prod"})
public class OppfølgingAvsluttetConsumer {

    private KandidatService kandidatService;
    private ConsumerProps consumerProps;

    public OppfølgingAvsluttetConsumer(KandidatService kandidatService, ConsumerProps consumerProps) {
        this.kandidatService = kandidatService;
        this.consumerProps = consumerProps;
    }

    @KafkaListener(topics = "#{consumerProps.getTopics()}")
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        try {
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = deserialiserMelding(melding.value());
            kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);

        } catch (IOException e) {
            log.error("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
        }
    }
}
