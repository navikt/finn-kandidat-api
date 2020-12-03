package no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(
            @Qualifier("consumerFactory") ConsumerFactory<String, String> consumerFactory
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
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }
}
