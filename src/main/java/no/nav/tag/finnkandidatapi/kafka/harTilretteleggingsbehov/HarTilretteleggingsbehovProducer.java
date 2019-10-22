package no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.Unleash;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.tag.finnkandidatapi.unleash.UnleashConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
@Slf4j
public class HarTilretteleggingsbehovProducer {

    private static final String HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS = "finnkandidat.hartilretteleggingsbehov.suksess";
    private static final String HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET = "finnkandidat.hartilretteleggingsbehov.feilet";

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private MeterRegistry meterRegistry;
    private Unleash unleash;

    public HarTilretteleggingsbehovProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}") String topic,
            MeterRegistry meterRegistry,
            Unleash unleash) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        this.unleash = unleash;
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS);
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET);
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        sendKafkamelding(event.getKandidat().getAktørId(), true);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        sendKafkamelding(event.getAktørId(), false);
    }

    public void sendKafkamelding(String aktørId, Boolean harTilretteleggingsbehov) {
        if (!unleash.isEnabled(UnleashConfiguration.HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE)) {
            log.info("Har tilretteleggingsbehov produsent er slått av, skulle publisere aktørId: {}", aktørId);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            HarTilretteleggingsbehov melding = new HarTilretteleggingsbehov(aktørId, harTilretteleggingsbehov);
            String serialisertMelding = mapper.writeValueAsString(melding);

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    topic,
                    aktørId,
                    serialisertMelding
            );

            future.addCallback(result -> {
                log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic, aktørId: {}", aktørId);
                meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS).increment();
            },
            exception -> {
                log.error("Kunne ikke sende kandidat på Kafka-topic, aktørId: {}", aktørId, exception);
                meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET).increment();
            });

        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere HarTilretteleggingsbehov", e);
            meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET).increment();
        }
    }
}
