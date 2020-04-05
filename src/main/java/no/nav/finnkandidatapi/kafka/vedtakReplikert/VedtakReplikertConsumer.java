package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikertUtils.deserialiserMelding;

@Slf4j
@Component
public class VedtakReplikertConsumer {

    private static final String REPLIKERTVEDTAK_FEILET = "finnkandidat.replikertvedtak.feilet";

    private VedtakService vedtakService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private VedtakReplikertConfig vedtakReplikertConfig;
    private MeterRegistry meterRegistry;

    public VedtakReplikertConsumer(
            VedtakService vedtakService,
            VedtakReplikertConfig vedtakReplikertConfig,
            MeterRegistry meterRegistry
    ) {
        this.vedtakService = vedtakService;
        this.vedtakReplikertConfig = vedtakReplikertConfig;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(REPLIKERTVEDTAK_FEILET);
    }

    @KafkaListener(
            topics = "#{vedtakReplikertConfig.getTopic()}",
            groupId = "finn-kandidat-vedtak-replikert",
            clientIdPrefix = "vedtak-replikert"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer vedtak replikert melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            String json = melding.value();
            log.info("Vedtak endret melding mottatt, json: {} ", json);
            VedtakReplikert vedtakReplikert = deserialiserMelding(json);
            log.info("Vedtak endret melding mottatt, java: {} ", vedtakReplikert);
            vedtakService.behandleVedtakReplikert(vedtakReplikert);

        } catch (RuntimeException e) {
            meterRegistry.counter(REPLIKERTVEDTAK_FEILET).increment();
            log.error("Feil ved konsumering av vedtak replikert melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }
}
