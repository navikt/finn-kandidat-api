package no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetUtils.deserialiserMelding;

@Slf4j
@Component
public class OppfølgingAvsluttetConsumer {

    private static final String AVSLUTTET_OPPFØLGING_FEILET = "finnkandidat.avsluttetoppfolging.feilet";

    private KandidatService kandidatService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig;
    private MeterRegistry meterRegistry;

    public OppfølgingAvsluttetConsumer(
            KandidatService kandidatService,
            OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig,
            MeterRegistry meterRegistry
    ) {
        this.kandidatService = kandidatService;
        this.oppfolgingAvsluttetConfig = oppfolgingAvsluttetConfig;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(AVSLUTTET_OPPFØLGING_FEILET);
    }

    @KafkaListener(topics = "#{oppfolgingAvsluttetConfig.getTopic()}", groupId = "finn-kandidat-oppfolging-avsluttet")
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer avsluttet oppfølging melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = deserialiserMelding(melding.value());
            kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);

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
}
