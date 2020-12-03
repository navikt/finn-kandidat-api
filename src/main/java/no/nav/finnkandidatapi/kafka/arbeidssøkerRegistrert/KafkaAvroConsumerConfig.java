package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@EnableKafka
@Configuration
@Profile("!local")
public class KafkaAvroConsumerConfig {

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> avroKafkaListenerContainerFactory(
            @Qualifier("avroConsumerFactory") ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);

        ExponentialBackOff backOff = new ExponentialBackOff(2000, 20);
        backOff.setMaxInterval(172800000);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(backOff));
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> avroConsumerFactory(KafkaProperties properties) {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        consumerProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
        consumerProperties.put(ErrorHandlingDeserializer.VALUE_FUNCTION, FaultyArbeidssokerRegistrertProvider.class);
        consumerProperties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }
}
