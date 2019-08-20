package no.nav.tag.finnkandidatapi.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetUtils.deserialiserMelding;

@Slf4j
@Component
public class OppfølgingAvsluttetConsumer {

    public static final String AVSLUTTET_OPPFØLGING_FEILET = "finnkandidat.avsluttetoppfolging.feilet";

    private KandidatService kandidatService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private ConsumerProps consumerProps;
    private MeterRegistry meterRegistry;

    public OppfølgingAvsluttetConsumer(
            KandidatService kandidatService,
            ConsumerProps consumerProps,
            MeterRegistry meterRegistry
    ) {
        this.kandidatService = kandidatService;
        this.consumerProps = consumerProps;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(AVSLUTTET_OPPFØLGING_FEILET);
    }

    @KafkaListener(topics = "#{consumerProps.getTopic()}", groupId = "finn-kandidat")
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
