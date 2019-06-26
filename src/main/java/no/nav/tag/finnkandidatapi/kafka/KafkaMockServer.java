package no.nav.tag.finnkandidatapi.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.stereotype.Component;

@Profile("local")
@Slf4j
@Component
@Getter
public class KafkaMockServer implements DisposableBean {

    private final EmbeddedKafkaBroker embeddedKafka;

    public KafkaMockServer(ConsumerProps consumerProps) {
        log.info("Starter embedded Kafka");
        embeddedKafka = new EmbeddedKafkaBroker(1, true, 1, consumerProps.getTopic());
        embeddedKafka.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        log.info("Stopper embedded Kafka");
        embeddedKafka.destroy();
    }
}
