package no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OppfølgingAvsluttetConsumer implements ConsumerSeekAware {

    private static final String AVSLUTTET_OPPFØLGING_FEILET = "finnkandidat.avsluttetoppfolging.feilet";

    private KandidatService kandidatService;
    private PermittertArbeidssokerService permittertArbeidssokerService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig;
    private MeterRegistry meterRegistry;

    public OppfølgingAvsluttetConsumer(
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService,
            OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig,
            MeterRegistry meterRegistry
    ) {
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.oppfolgingAvsluttetConfig = oppfolgingAvsluttetConfig;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(AVSLUTTET_OPPFØLGING_FEILET);
    }

    @KafkaListener(
            topics = "#{oppfolgingAvsluttetConfig.getTopic()}",
            groupId = "finn-kandidat-oppfolging-avsluttet",
            clientIdPrefix = "oppfolging-avsluttet",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer avsluttet oppfølging melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = OppfølgingAvsluttetUtils.deserialiserMelding(melding.value());
            kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);
            permittertArbeidssokerService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);

        } catch (RuntimeException e) {
            meterRegistry.counter(AVSLUTTET_OPPFØLGING_FEILET).increment();
            log.error("Feil ved konsumering av avsluttet oppfølging melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.forEach((t, o) -> callback.seekToBeginning(t.topic(), t.partition()));
    }

}
