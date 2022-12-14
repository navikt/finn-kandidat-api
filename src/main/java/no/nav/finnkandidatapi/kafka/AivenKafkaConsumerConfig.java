package no.nav.finnkandidatapi.kafka;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@EnableKafka
@Configuration
public class AivenKafkaConsumerConfig {

    @Value("${KAFKA_BROKERS:http://dummyurl.com:0000}")
    private String brokersUrl;

    @Value("${KAFKA_KEYSTORE_PATH:}")
    private String keystorePath;

    @Value("${KAFKA_TRUSTSTORE_PATH:}")
    private String truststorePath;

    @Value("${KAFKA_CREDSTORE_PASSWORD:}")
    private String credstorePassword;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> aivenKafkaListenerContainerFactory(
            @Qualifier("aivenConsumerFactory") ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);

        // 20 sek, 400 sek, ~2 timer, ~2 dager
        ExponentialBackOff backOff = new ExponentialBackOff(2000, 20);
        backOff.setMaxInterval(172800000);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(backOff));

        return factory;
    }

    @Bean
    @Profile("!local")
    public ConsumerFactory<String, String> aivenConsumerFactory(KafkaProperties properties) {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();

        consumerProperties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        consumerProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokersUrl);

        if (StringUtils.isNotEmpty(keystorePath)) {
            consumerProperties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword);
            consumerProperties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword);
            consumerProperties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
            consumerProperties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
        }

        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }
}
