package no.nav.tag.finnkandidatapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kafka.ConsumerProps;
import no.nav.tag.finnkandidatapi.kafka.KafkaMockServer;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"local", "mock"})
@DirtiesContext
@Slf4j
public class OppfølgingAvsluttetConsumerTest {

    private static final String AKTØR_ID = "1856024171652";

    @Autowired
    private ConsumerProps consumerTopicProps;

    @Autowired
    private KafkaMockServer embeddedKafka;

    private Producer<String, String> producer;

    private ConcurrentMessageListenerContainer<String, String> container;

    @Autowired
    private KandidatRepository repository;

    @Before
    public void setUp() {
        // Config kopiert fra KafkaConfig
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        producer = pf.createProducer();

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(1);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(4));
        factory.setStatefulRetry(true);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(20000);
        backOffPolicy.setMultiplier(20);
        backOffPolicy.setMaxInterval(172800000);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        factory.setRetryTemplate(retryTemplate);

        container = factory.createContainer(consumerTopicProps.getTopic());
        container.setupMessageListener((MessageListener<String, String>) record -> {
            log.info("KafkaMessage: {}", record);
        });
        container.start();

        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
    }

    @Test(timeout = 2000)
    @SneakyThrows
    public void skal_slette_kandidat_ved_mottatt_oppfølging_avsluttet_kafka_melding() {
        Kandidat kandidatSomSkalSlettes = enKandidat();
        kandidatSomSkalSlettes.setAktørId(AKTØR_ID);
        repository.lagreKandidat(kandidatSomSkalSlettes);
        sendOppFølgingAvsluttetMelding();

        boolean kandidatErslettet = false;
        while(!kandidatErslettet) {
            Thread.sleep(10);
            kandidatErslettet = repository.hentNyesteKandidat(kandidatSomSkalSlettes.getAktørId()).isEmpty();
        }
        assertThat(kandidatErslettet).isTrue();
    }

    private void sendOppFølgingAvsluttetMelding() throws JsonProcessingException {
        String melding = lagOppfølgingAvsluttetMelding(AKTØR_ID);
        producer.send(new ProducerRecord<>(consumerTopicProps.getTopic(), "123", melding));
    }

    private String lagOppfølgingAvsluttetMelding(String aktørId) throws JsonProcessingException {
        OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = OppfølgingAvsluttetMelding.builder()
                .aktørId(aktørId)
                .sluttdato(new Date()).build();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(oppfølgingAvsluttetMelding);
    }

    @After
    public void tearDown() {
        if (container != null) {
            container.stop();
        }
    }
}
