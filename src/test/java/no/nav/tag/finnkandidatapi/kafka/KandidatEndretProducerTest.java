package no.nav.tag.finnkandidatapi.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static no.nav.tag.finnkandidatapi.TestData.enAktørId;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles({"local", "mock"})
public class KandidatEndretProducerTest {

    @Autowired
    private KafkaMockServer embeddedKafka;

    @Autowired
    private KandidatEndretProducer kandidatEndretProducer;

    @Autowired
    private DefaultKafkaConsumerFactory<String, InkluderingsKandidat> mockKafkaConsumerFactory;

    private Consumer<String, InkluderingsKandidat> consumer;

    @Before
    public void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "http://ikkeibruk.no");

        consumer = mockKafkaConsumerFactory.createConsumer();
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, "aapen-tag-kandidatEndret-v1-default");
    }

    @Test
    public void kandidatEndret__skal_sende_melding_på_kafka_topic() {
        InkluderingsKandidat kandidat = new InkluderingsKandidat(enAktørId(), true);
        kandidatEndretProducer.kandidatEndret(kandidat.getAktørId().toString(), kandidat.getHarTilretteleggingsbehov());

        ConsumerRecord<String, InkluderingsKandidat> melding = KafkaTestUtils.getSingleRecord(consumer, "aapen-tag-kandidatEndret-v1-default");

        assertThat(melding.key()).isEqualTo(kandidat.getAktørId().toString());
        assertThat(melding.value()).isEqualTo(kandidat);
    }

    @After
    public void tearDown() {
        embeddedKafka.destroy();
    }
}
