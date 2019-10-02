package no.nav.tag.finnkandidatapi.kafka.config;

import no.nav.tag.finnkandidatapi.kafka.InkluderingsKandidat;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    // TODO: Fiks bean her
    @Bean
    public ProducerFactory<String, InkluderingsKandidat> producerFactory(KafkaProperties properties) {
        // Set deserializer og serializer her og ikke i config hvor det alltid blir lastet?
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, InkluderingsKandidat> kafkaTemplate(ProducerFactory<String, InkluderingsKandidat> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
