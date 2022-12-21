package no.nav.finnkandidatapi.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;


@Configuration
public class AivenKafkaConsumerConfigTest {

    @Bean
    @Profile("local")
    public ConsumerFactory<String, String> aivenConsumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }
}
