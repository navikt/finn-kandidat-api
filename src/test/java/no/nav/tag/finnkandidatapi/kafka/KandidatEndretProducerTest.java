package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
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

    private Consumer<String, String> consumer;

    @Before
    public void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, "aapen-tag-kandidatEndret-v1-default");
    }

    @Test
    public void kandidatEndret__skal_sende_melding_på_kafka_topic() {
        Kandidat kandidat = enKandidat();
        kandidatEndretProducer.kandidatEndret(kandidat.getAktørId(), true);

        ConsumerRecord<String, String> melding = KafkaTestUtils.getSingleRecord(consumer, "aapen-tag-kandidatEndret-v1-default");
        assertThat(melding.key()).isEqualTo(kandidat.getAktørId());
        assertThat(melding.value()).isEqualTo("true");
    }

    @After
    public void rivNed() {
        embeddedKafka.destroy();
    }
}
