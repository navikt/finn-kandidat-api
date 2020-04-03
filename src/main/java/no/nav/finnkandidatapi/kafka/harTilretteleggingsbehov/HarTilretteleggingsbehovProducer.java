package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.Unleash;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private KandidatService kandidatService;
    private PermittertArbeidssokerService permittertArbeidssokerService;

    public HarTilretteleggingsbehovProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}" ) String topic,
            MeterRegistry meterRegistry,
            Unleash unleash, FeatureToggleService featureToggleService,
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        this.featureToggleService = featureToggleService;
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_SUKSESS);
        meterRegistry.counter(HAR_TILRETTELEGGINGSBEHOV_PRODUSENT_FEILET);
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
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(kandidat.getAktørId());
        List<String> kategorier = kandidat.kategorier();

        lagOgSendMelding(kandidat.getAktørId(), kategorier, permittertArbeidssoker);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        List<String> kategorier = new ArrayList<>();
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(event.getAktørId());
        if (permittertArbeidssoker.isPresent() && permittertArbeidssoker.get().erPermittert()) {
            kategorier.add(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI);
        }
        HarTilretteleggingsbehov melding = new HarTilretteleggingsbehov(event.getAktørId(), false, kategorier);
        sendKafkamelding(melding);
    }

    @EventListener
    public void permitteringEndretEllerOpprettet(PermittertArbeidssokerEndretEllerOpprettet event) {
        PermittertArbeidssoker permittertArbeidssoker = event.getPermittertArbeidssoker();
        Optional<Kandidat> kandidat = kandidatService.hentNyesteKandidat(permittertArbeidssoker.getAktørId());
        List<String> kategorier = kandidat.map(Kandidat::kategorier).orElse(Collections.emptyList());

        lagOgSendMelding(permittertArbeidssoker.getAktørId(), kategorier, Optional.of(permittertArbeidssoker));
    }

    private void lagOgSendMelding(String aktørId, List<String> kategorier, Optional<PermittertArbeidssoker> permittertArbeidssoker) {
        boolean harBehov = !kategorier.isEmpty();
        List<String> kategorierOgPermittering = kombiner(kategorier, permittertArbeidssoker);
        HarTilretteleggingsbehov melding = new HarTilretteleggingsbehov(aktørId, harBehov, kategorierOgPermittering);
        sendKafkamelding(melding);
    }

    private List<String> kombiner(List<String> kategorier, Optional<PermittertArbeidssoker> permittertArbeidssoker) {
        List<String> kombinert = new ArrayList<>(kategorier);
        if (permittertArbeidssoker.isPresent() && permittertArbeidssoker.get().erPermittert()) {
            kombinert.add(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI);
        }
        return kombinert;
    }

    public void sendKafkamelding(HarTilretteleggingsbehov melding) {
        log.info("Lager melding med følgende behov: {}", melding.getBehov());
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
