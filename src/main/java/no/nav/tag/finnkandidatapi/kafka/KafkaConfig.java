package no.nav.tag.finnkandidatapi.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    // TODO: Kjør samme Kafka lokalt og i tester
    // TODO: Skal denne også kjøres i test?

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(4));
        factory.setStatefulRetry(true);

        // 20 sek, 400 sek, ~2 timer, ~2 dager
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(20000);
        backOffPolicy.setMultiplier(20);
        backOffPolicy.setMaxInterval(172800000);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        factory.setRetryTemplate(retryTemplate);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    // TODO: Fjern alt nedenfor her
    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaProperties properties) {
        Map<String, Object> producerProps = properties.buildProducerProperties();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
