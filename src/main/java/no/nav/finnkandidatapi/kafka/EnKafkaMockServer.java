package no.nav.finnkandidatapi.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfolgingAvsluttetConfig;
import no.nav.finnkandidatapi.kafka.oppfølgingEndret.OppfolgingEndretConfig;
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
    public final String topicName = "aapen-tag-kandidatEndret-v1-default";

    private final EmbeddedKafkaBroker embeddedKafka;

    /*
        TODO: Fikse bug med initialiseringsrekkefølge i Spring
        HarTilretteleggingsbehov må initialiseres etter denne klassen,
        og må dermed hete noe etter denne klassen i alfabetet siden
        Spring initialiserer komponenter i alfabetisk rekkefølge hvis
        de ikke har noen synlige avhengigheter.
     */
    public EnKafkaMockServer(
            OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig,
            OppfolgingEndretConfig oppfolgingEndretConfig,
            VedtakReplikertConfig vedtakReplikertConfig) {
        log.info("Starter embedded Kafka");
        embeddedKafka = new EmbeddedKafkaBroker(
                1,
                true,
                1,
                oppfolgingAvsluttetConfig.getTopic(),
                oppfolgingEndretConfig.getTopic(),
                vedtakReplikertConfig.getTopic(),
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
