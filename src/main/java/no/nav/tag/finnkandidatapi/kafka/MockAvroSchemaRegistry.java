package no.nav.tag.finnkandidatapi.kafka;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

@Profile("mock")
@Configuration
class MockAvroSchemaRegistry {
    // KafkaProperties groups all properties prefixed with `spring.kafka`

//    private KafkaProperties props;
//    public MockAvroSchemaRegistry(KafkaProperties props) {
//        this.props = props;
//    }

    /**
     * Mock schema registry bean used by Kafka Avro Serde since
     * the @EmbeddedKafka setup doesn't include a schema registry.
     * @return MockSchemaRegistryClient instance
     */
    @Bean
    SchemaRegistryClient schemaRegistryClient() {
        return new MockSchemaRegistryClient();
    }

    /**
     * KafkaAvroSerializer that uses the MockSchemaRegistryClient
     * @return KafkaAvroSerializer instance
     */
    @Bean
    KafkaAvroSerializer kafkaAvroSerializer(SchemaRegistryClient schemaRegistryClient) {
        return new KafkaAvroSerializer(schemaRegistryClient);
    }

    /**
     * KafkaAvroDeserializer that uses the MockSchemaRegistryClient.
     * The props must be provided so that specific.avro.reader: true
     * is set. Without this, the consumer will receive GenericData records.
     * @return KafkaAvroDeserializer instance
     */
    @Bean
    KafkaAvroDeserializer kafkaAvroDeserializer(
            SchemaRegistryClient schemaRegistryClient,
            KafkaProperties properties
    ) {
        return new KafkaAvroDeserializer(schemaRegistryClient, properties.buildConsumerProperties());
    }

    /**
     * Configures the kafka producer factory to use the overridden
     * KafkaAvroDeserializer so that the MockSchemaRegistryClient
     * is used rather than trying to reach out via HTTP to a schema registry
     * @return DefaultKafkaProducerFactory instance
     */
    @Primary
    @Bean
    DefaultKafkaProducerFactory producerFactory2(
            KafkaAvroSerializer kafkaAvroSerializer,
            KafkaProperties properties
    ) {
        return new DefaultKafkaProducerFactory(
                properties.buildProducerProperties(),
                new StringSerializer(),
                kafkaAvroSerializer
        );
    }

    /**
     * Configures the kafka consumer factory to use the overridden
     * KafkaAvroSerializer so that the MockSchemaRegistryClient
     * is used rather than trying to reach out via HTTP to a schema registry
     * @return DefaultKafkaConsumerFactory instance
     */
    // TODO: endre navn
    @Primary
    @Bean
    DefaultKafkaConsumerFactory consumerFactory2(
            KafkaAvroDeserializer kafkaAvroDeserializer,
            KafkaProperties properties
    ) {
        return new DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                kafkaAvroDeserializer
        );
    }

    /**
     * Configure the ListenerContainerFactory to use the overridden
     * consumer factory so that the MockSchemaRegistryClient is used
     * under the covers by all consumers when deserializing Avro data.
     * @return ConcurrentKafkaListenerContainerFactory instance
     */
    // TODO: Fikse navn
    @Bean
    ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory2(
            ConsumerFactory consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
