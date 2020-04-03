package no.nav.finnkandidatapi.kafka.vedtakEndret;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.kafka.vedtakEndret.VedtakEndretUtils.deserialiserMelding;

@Slf4j
@Component
public class VedtakEndretConsumer {

    private static final String ENDRET_VEDTAK_FEILET = "finnkandidat.endretvedtak.feilet";

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private VedtakEndretConfig vedtakEndretConfig;
    private MeterRegistry meterRegistry;

    public VedtakEndretConsumer(
            VedtakEndretConfig vedtakEndretConfig,
            MeterRegistry meterRegistry
    ) {
        this.vedtakEndretConfig = vedtakEndretConfig;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(ENDRET_VEDTAK_FEILET);
    }

    @KafkaListener(
            topics = "#{vedtakEndretConfig.getTopic()}",
            groupId = "finn-kandidat-vedtak-endret",
            clientIdPrefix = "vedtak-endret"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer vedtak endret melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            String json = melding.value();
            log.info("Vedtak endret melding mottatt, json: {} ", json);
            VedtakEndret vedtakEndret = deserialiserMelding(json);
            log.info("Vedtak endret melding mottatt, java: {} ", vedtakEndret);
            throw new RuntimeException("Alt funka, men ruller tilbake likevel");
        } catch (RuntimeException e) {
            meterRegistry.counter(ENDRET_VEDTAK_FEILET).increment();
            log.error("Feil ved konsumering av vedtak endret melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }
}
