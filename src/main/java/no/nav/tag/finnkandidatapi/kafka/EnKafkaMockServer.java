package no.nav.tag.finnkandidatapi.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfolgingAvsluttetConfig;
import no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret.OppfolgingEndretConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.stereotype.Component;

@Profile("local")
@Slf4j
@Component
@Getter
public class EnKafkaMockServer implements DisposableBean {

    private final EmbeddedKafkaBroker embeddedKafka;

    /*
        TODO: Fikse bug med initialiseringsrekkefølge i Spring
        HarTilretteleggingsbehov må initialiseres etter denne klassen,
        og må dermed hete noe etter denne klassen i alfabetet siden
        Spring initialiserer komponenter i alfabetisk rekkefølge hvis
        de ikke har noen synlige avhengigheter.
     */
    public EnKafkaMockServer(OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig, OppfolgingEndretConfig oppfolgingEndretConfig) {
        log.info("Starter embedded Kafka");
        embeddedKafka = new EmbeddedKafkaBroker(
                1,
                true,
                1,
                oppfolgingAvsluttetConfig.getTopic(),
                oppfolgingEndretConfig.getTopic(),
                "aapen-tag-kandidatEndret-v1-default"
        );
        embeddedKafka.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        log.info("Stopper embedded Kafka");
        embeddedKafka.destroy();
    }
}
