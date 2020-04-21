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
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.unleash.FeatureToggleService;
import no.nav.finnkandidatapi.vedtak.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.finnkandidatapi.kafka.midlertidigutilgjengelig.MidlertidigTilretteleggingsbehovProducer.finnMidlertidigUtilgjengeligFilter;
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
    private VedtakService vedtakService;
    private MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    public HarTilretteleggingsbehovProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}") String topic,
            MeterRegistry meterRegistry,
            Unleash unleash, FeatureToggleService featureToggleService,
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService,
            MidlertidigUtilgjengeligService midlertidigUtilgjengeligService) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        this.featureToggleService = featureToggleService;
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
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
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(vedtak.getAktørId());
        Optional<Kandidat> kandidat = kandidatService.hentNyesteKandidat(vedtak.getAktørId());
        List<String> kategorier = kandidat.map(Kandidat::kategorier).orElse(Collections.emptyList());
        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(vedtak.getAktørId());
        lagOgSendMelding(vedtak.getAktørId(), kategorier, permittertArbeidssoker, Optional.of(vedtak), midlertidigUtilgjengelig);
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
        Optional<Vedtak> vedtak = vedtakService.hentNyesteVedtakForAktør(kandidat.getAktørId());
        List<String> kategorier = kandidat.kategorier();
            Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(kandidat.getAktørId());
        lagOgSendMelding(kandidat.getAktørId(), kategorier, permittertArbeidssoker, vedtak, midlertidigUtilgjengelig);
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
        Optional<Vedtak> vedtak = vedtakService.hentNyesteVedtakForAktør(permittertArbeidssoker.getAktørId());
        List<String> kategorier = kandidat.map(Kandidat::kategorier).orElse(Collections.emptyList());
        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig = midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(permittertArbeidssoker.getAktørId());

        lagOgSendMelding(permittertArbeidssoker.getAktørId(), kategorier, Optional.of(permittertArbeidssoker), vedtak, midlertidigUtilgjengelig);
    }

    public void lagOgSendMelding(String aktørId,
                                 List<String> kategorier,
                                 Optional<PermittertArbeidssoker> permittertArbeidssoker,
                                 Optional<Vedtak> vedtak,
                                 Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig
    ) {
        boolean harBehov = !kategorier.isEmpty();
        boolean erPermittert = SjekkPermittertUtil.sjekkOmErPermittert(permittertArbeidssoker, vedtak);
        List<String> filtre = kombiner(kategorier, erPermittert, midlertidigUtilgjengelig);
        HarTilretteleggingsbehov melding = new HarTilretteleggingsbehov(aktørId, harBehov, filtre);

        sendKafkamelding(melding);
    }

    private List<String> kombiner(List<String> kategorier, boolean erPermittert, Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig) {
        List<String> kombinert = new ArrayList<>(kategorier);
        if (erPermittert) {
            kombinert.add(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI);
        }
        Optional<String> midlertidigUtilgjengeligFilter = finnMidlertidigUtilgjengeligFilter(midlertidigUtilgjengelig);

        return Stream.concat(
                kombinert.stream(),
                midlertidigUtilgjengeligFilter.stream()
        ).collect(Collectors.toList());
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
