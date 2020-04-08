package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import no.nav.metrics.MetricsFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikertUtils.deserialiserMelding;

@Slf4j
@Component
public class VedtakReplikertConsumer {

    private static final String REPLIKERTVEDTAK_FEILET = "finnkandidat.replikertvedtak.feilet";

    private VedtakService vedtakService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private VedtakReplikertConfig vedtakReplikertConfig;
    private MeterRegistry meterRegistry;
    private AtomicInteger teller = new AtomicInteger(0);

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
            groupId = "finn-kandidat-vedtak-replikert2",
            clientIdPrefix = "vedtak-replikert"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer vedtak replikert melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );
        String json = melding.value();
        log.info("Vedtak replikert meldingen: {}", json);
        try {
            VedtakReplikert vedtakReplikert = deserialiserMelding(json);
            log.info("Vedtak replikert melding mottatt, java: {} ", vedtakReplikert);

            MetricsFactory.createEvent("finn-kandidat.vedtak.mottatt" )
                    .addTagToReport("operasjon", vedtakReplikert.getOp_type())
                    .report();

            vedtakService.behandleVedtakReplikert(vedtakReplikert);
            int tellerVerdi = teller.incrementAndGet();
            if( tellerVerdi > 10 ) {
                throw new RuntimeException("Ikke les flere meldinger..");
            }

        } catch (RuntimeException e) {
            meterRegistry.counter(REPLIKERTVEDTAK_FEILET).increment();
            log.error("Feil ved konsumering av vedtak replikert melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            log.error("Vedtak replikert meldingen som feilet: {}", json);
            throw e;
        }
    }
}
