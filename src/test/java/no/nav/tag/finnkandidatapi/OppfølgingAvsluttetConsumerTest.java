package no.nav.tag.finnkandidatapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Map;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"kafka-test", "local", "mock"})
@DirtiesContext
@Slf4j
public class OppfølgingAvsluttetConsumerTest {

    private static final String OPPFØLGING_AVSLUTTET_TOPIC = "test-topic";

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OPPFØLGING_AVSLUTTET_TOPIC);

    private static Producer<String, String> producer;

    private static KafkaMessageListenerContainer<String, String> container;

    @Autowired
    private KandidatRepository repository;

    @BeforeClass
    public static void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        producer = pf.createProducer();

        ContainerProperties containerProperties = new ContainerProperties(OPPFØLGING_AVSLUTTET_TOPIC);
        container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("KafkaMessage: {}", record);
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
    }

    @Test(timeout = 1000)
    @SneakyThrows
    public void skal_slette_kandidat_ved_mottatt_oppfølging_avsluttet_kafka_melding() {
        Kandidat kandidatSomSkalSlettes = enKandidat();
        kandidatSomSkalSlettes.setFnr("01065500791");
        repository.lagreKandidat(kandidatSomSkalSlettes);
        sendOppFølgingAvsluttetMelding();

        boolean kandidatErslettet = false;
        while(!kandidatErslettet) {
            Thread.sleep(10);
            kandidatErslettet = repository.hentNyesteKandidat(kandidatSomSkalSlettes.getFnr()).isEmpty();
        }
        assertThat(kandidatErslettet).isTrue();
    }

//    TODO: Test for å sjekke at man konsumerer melding på ny hvis den feiler
//    @Test
//    @SneakyThrows
//    public void skal_konsumere_melding_igjen_hvis_feil_ved_deserialisering() {
//        // Produser melding med feil format
//        String melding = "JSON som ikke kan deserialiseres";
//        producer.send(new ProducerRecord<>(OPPFØLGING_AVSLUTTET_TOPIC, "666", melding));
//        sendOppFølgingAvsluttetMelding();
//
//          Prøv å konsumere meldingen
//          RuntimeException skal kastes
//          Send ny melding (kanskje med riktig format på melding)
//          Prøv å konsumere igjen
//          Konsume metoden skal kjøres to ganger
//          Sjekk om casen over skjer hvis man tar bort SeekToCurrentErrorHandler
//    }

    private void sendOppFølgingAvsluttetMelding() throws JsonProcessingException {
        String aktørId = "1856024171652";
        String melding = lagOppfølgingAvsluttetMelding(aktørId);
        producer.send(new ProducerRecord<>(OPPFØLGING_AVSLUTTET_TOPIC, "123", melding));
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

    @After
    public void tearDown() {
        if (container != null) {
            container.stop();
        }
    }
}
