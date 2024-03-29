package no.nav.finnkandidatapi.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode.OppfolgingsperiodeConfig;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikertConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.stereotype.Component;

@Profile("local")
@Slf4j
@Component
@Getter
public class EnKafkaMockServer implements DisposableBean {
    public final String topicName = "toi.tillretteleggingsbehov-1";

    private final EmbeddedKafkaBroker embeddedKafka;

    /*
        HarTilretteleggingsbehov må initialiseres etter denne klassen,
        og må dermed hete noe etter denne klassen i alfabetet siden
        Spring initialiserer komponenter i alfabetisk rekkefølge hvis
        de ikke har noen synlige avhengigheter.
     */
    public EnKafkaMockServer(
            VedtakReplikertConfig vedtakReplikertConfig,
            OppfolgingsperiodeConfig oppfolgingsperiodeConfig) {
        log.info("Starter embedded Kafka");
        embeddedKafka = new EmbeddedKafkaBroker(
                1,
                true,
                1,
                vedtakReplikertConfig.getTopic(),
                oppfolgingsperiodeConfig.getTopic(),
                topicName
        );
        embeddedKafka.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        log.info("Stopper embedded Kafka");
        embeddedKafka.destroy();
    }
}
