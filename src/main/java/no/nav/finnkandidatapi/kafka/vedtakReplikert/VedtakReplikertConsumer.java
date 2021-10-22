package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikertUtils.deserialiserMelding;

@Slf4j
@Component
public class VedtakReplikertConsumer {

    private VedtakService vedtakService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private VedtakReplikertConfig vedtakReplikertConfig;

    public VedtakReplikertConsumer(
            VedtakService vedtakService,
            VedtakReplikertConfig vedtakReplikertConfig
    ) {
        this.vedtakService = vedtakService;
        this.vedtakReplikertConfig = vedtakReplikertConfig;
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
        try {
            VedtakReplikert vedtakReplikert = deserialiserMelding(json);

            log.debug("Skal behandle vedtak", json);
            vedtakService.behandleVedtakReplikert(vedtakReplikert);

        } catch (RuntimeException e) {
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
