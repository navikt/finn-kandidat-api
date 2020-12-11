package no.nav.finnkandidatapi.kafka.samtykke;

import org.apache.kafka.clients.consumer.ConsumerRecord;

class SamtykkeConsumerTest {

    private ConsumerRecord<String, String> getConsumerRecord() {
        String key = "1000057411798";
        String value = "{\"aktoerId\":\"1000068432771\",\"fnr\":\"27075349594\",\"meldingType\":\"SAMTYKKE_OPPRETTET\",\"ressurs\":\"CV_HJEMMEL\",\"opprettetDato\":\"2019-01-09T12:36:06+01:00\",\"slettetDato\":null,\"versjon\":1,\"versjonGjeldendeFra\":null,\"versjonGjeldendeTil\":null}";
        return new ConsumerRecord<>("topic", 0, 0, key, value);
    }
}