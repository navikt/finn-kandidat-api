package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
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
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@EnableKafka
@Configuration
@Profile("!local")
public class KafkaAvroConsumerConfig {

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> avroKafkaListenerContainerFactory(
            @Qualifier("avroConsumerFactory") ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = configureFactory(consumerFactory);
        ExponentialBackOffPolicy backOffPolicy = configureBackOffPolicy();
        RetryTemplate retryTemplate = configureRetryTemplate(backOffPolicy);

        factory.setRetryTemplate(retryTemplate);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> avroConsumerFactory(KafkaProperties properties) {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        consumerProperties.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
        consumerProperties.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FaultyArbeidssokerRegistrertProvider.class);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    private RetryTemplate configureRetryTemplate(ExponentialBackOffPolicy backOffPolicy) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> configureFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(4));
        factory.setStatefulRetry(true);

        return factory;
    }

    private ExponentialBackOffPolicy configureBackOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();

        // 20 sek, 400 sek, ~2 timer, ~2 dager
        backOffPolicy.setInitialInterval(20000);
        backOffPolicy.setMultiplier(20);
        backOffPolicy.setMaxInterval(172800000);

        return backOffPolicy;
    }

}
