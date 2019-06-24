package no.nav.tag.finnkandidatapi.kafka;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Profile({"kafka-test", "dev", "prod"})
public class OppfølgingAvsluttetConsumer {

    private KandidatService kandidatService;
    private ConsumerProps consumerProps;

    @KafkaListener(topics = "#{consumerProps.getTopic()}")
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        try {
            log.info(
                    "Konsumerer avsluttet oppfølging melding for id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = deserialiserMelding(melding.value());
            kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);

        } catch (IOException e) {
            // TODO:
            //  Ha overvåkning på dette i Kibana board
            //  Skriv test for om dette feiler
            //  Sjekk om default er at melding blir konsumert igjen hvis noe feiler
            log.error("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
            throw new RuntimeException("Kunne ikke deserialisere OppfølgingAvsluttetMelding", e);
        }
    }
}
