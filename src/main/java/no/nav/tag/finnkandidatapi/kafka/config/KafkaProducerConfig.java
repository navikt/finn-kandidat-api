package no.nav.tag.finnkandidatapi.kafka.config;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
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

//    @Bean
    // TODO: Fiks bean her
    public ProducerFactory<String, InkluderingsKandidat> producerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, InkluderingsKandidat> kafkaTemplate(ProducerFactory<String, InkluderingsKandidat> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
