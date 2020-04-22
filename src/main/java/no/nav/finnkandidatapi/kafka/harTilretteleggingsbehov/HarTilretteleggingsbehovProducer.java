package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.Unleash;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakEndret;
import no.nav.finnkandidatapi.vedtak.VedtakOpprettet;
import no.nav.finnkandidatapi.vedtak.VedtakSlettet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.finnkandidatapi.unleash.UnleashConfiguration.HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE;

@Component
@Slf4j
public class HarTilretteleggingsbehovProducer {

    private static final String HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS = "finnkandidat.hartilretteleggingsbehov.suksess";
    private static final String HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET = "finnkandidat.hartilretteleggingsbehov.feilet";

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private MeterRegistry meterRegistry;
    private FeatureToggleService featureToggleService;
    private SammenstillBehov sammenstillBehov;

    public HarTilretteleggingsbehovProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}") String topic,
            MeterRegistry meterRegistry,
            Unleash unleash, FeatureToggleService featureToggleService,
            SammenstillBehov sammenstillBehov
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        this.featureToggleService = featureToggleService;
        this.sammenstillBehov = sammenstillBehov;
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS);
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET);
    }

    @EventListener
    public void vedtakOpprettet(VedtakOpprettet vedtakOpprettet) {
        mottattVedtakEvent(vedtakOpprettet.getVedtak());
    }

    @EventListener
    public void vedtakEndret(VedtakEndret vedtakEndret) {
        mottattVedtakEvent(vedtakEndret.getVedtak());
    }

    @EventListener
    public void vedtakSlettet(VedtakSlettet vedtakSlettet) {
        mottattVedtakEvent(vedtakSlettet.getVedtak());
    }

    private void mottattVedtakEvent(Vedtak vedtak) {
        sendKafkamelding(
                sammenstillBehov.lagbehov(vedtak)
        );
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        Kandidat kandidat = event.getKandidat();
        kandidatOpprettetEllerEndret(kandidat);
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        Kandidat kandidat = event.getKandidat();
        kandidatOpprettetEllerEndret(kandidat);
    }

    private void kandidatOpprettetEllerEndret(Kandidat kandidat) {
        HarTilretteleggingsbehov behov = sammenstillBehov.lagbehovKandidat(
                new HarTilretteleggingsbehov(
                        kandidat.getAktørId(),
                        CollectionUtils.isNotEmpty(kandidat.kategorier()),
                        kandidat.kategorier()));
        sendKafkamelding(behov);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        sendKafkamelding(
                sammenstillBehov.lagbehovKandidat(
                        new HarTilretteleggingsbehov(event.getAktørId(), false, Collections.emptyList()))
        );
    }

    @EventListener
    public void permitteringEndretEllerOpprettet(PermittertArbeidssokerEndretEllerOpprettet event) {
        sendKafkamelding(
                sammenstillBehov.lagbehov(event.getPermittertArbeidssoker())
        );
    }

    private <T> List<T> concatToList(Stream<T>... s) {
        return Arrays.stream(s).reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }

    public void sendKafkamelding(HarTilretteleggingsbehov melding) {
        String payload;
        try {
            payload = new ObjectMapper().writeValueAsString(melding);
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere HarTilretteleggingsbehov", e);
            meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET).increment();
            return;
        }
        send(melding.getAktoerId(), payload);
    }

    private void send(String key, String payload) {
        if (!featureToggleService.isEnabled(HAR_TILRETTELEGGINGSBEHOV_PRODUCER_FEATURE)) {
            log.info("Har tilretteleggingsbehov produsent er slått av, skulle publisere aktørId: {}", key);
            return;
        }

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);
        future.addCallback(result -> {
                    log.info(
                            "Kandidats behov for tilrettelegging sendt på Kafka-topic, aktørId: {}, offset: {}",
                            key,
                            result.getRecordMetadata().offset()
                    );
                    meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS).increment();
                },
                exception -> {
                    log.error("Kunne ikke sende kandidat på Kafka-topic, aktørId: {}", key, exception);
                    meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET).increment();
                });
    }
}
