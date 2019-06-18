package no.nav.tag.finnkandidatapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetConsumer;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetConsumer.OPPFØLGING_AVSLUTTET_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"kafka-test", "local", "mock"})
@DirtiesContext
public class OppfølgingAvsluttetConsumerTest {

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, 1, OPPFØLGING_AVSLUTTET_TOPIC);

    private Producer<String, String> producer;

    @Autowired
    private KandidatRepository repository;

    @Before
    public void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        producer = pf.createProducer();
    }

    @Test
    public void besvarelseMottatt__skal_sende_kontaktskjema_på_kafka_topic_med_riktige_felter() {
        Kandidat kandidatSomSkalSlettes = enKandidat();
        String fnr = "01065500791";
        kandidatSomSkalSlettes.setFnr(fnr);
        repository.lagreKandidat(kandidatSomSkalSlettes);

        try {
            String aktørId = "1856024171652";
            String melding = lagOppfølgingAvsluttetMelding(aktørId);
            producer.send(new ProducerRecord<>(OPPFØLGING_AVSLUTTET_TOPIC, "123", melding));

            Thread.sleep(1000);

            Optional<Kandidat> tomKandidat = repository.hentNyesteKandidat("01065500791");
            assertThat(tomKandidat).isEmpty();

        } catch (JsonProcessingException | InterruptedException e) {
            fail("Feilet", e);
        }
    }

    private String lagOppfølgingAvsluttetMelding(String aktørId) throws JsonProcessingException {
        OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = OppfølgingAvsluttetMelding.builder()
                .aktorId(aktørId)
                .sluttdato(LocalDateTime.now()).build();
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(oppfølgingAvsluttetMelding);
    }
}
