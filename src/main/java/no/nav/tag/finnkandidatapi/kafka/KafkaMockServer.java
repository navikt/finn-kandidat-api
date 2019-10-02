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
        embeddedKafka = new EmbeddedKafkaBroker(1, true, 1, consumerProps.getTopic(), "aapen-tag-kandidatEndret-v1-default");
        embeddedKafka.afterPropertiesSet();
    }

    // TODO: Flytte schemating inn her også?

    @Override
    public void destroy() {
        log.info("Stopper embedded Kafka");
        embeddedKafka.destroy();
    }
}
