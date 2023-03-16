package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.SecureLog.secureLog;
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
            groupId = "finn-kandidat-vedtak-replikert1",
            clientIdPrefix = "vedtak-replikert",
            containerFactory = "aivenKafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info("Konsumerer vedtak replikert melding, se securelog for detaljer");
        secureLog.info(
                "Konsumerer vedtak replikert melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );
        String json = melding.value();
        try {
            VedtakReplikert vedtakReplikert = deserialiserMelding(json);

            vedtakService.behandleVedtakReplikert(vedtakReplikert);

        } catch (RuntimeException e) {
            StringBuilder sb = new StringBuilder("Forsøkte å konsumere Kafka-melding vedtak replikert. ");
            sb.append("topic=" + vedtakReplikertConfig.getTopic());
            sb.append(", id=" + melding.key());
            sb.append(", offset=" + melding.offset());
            sb.append(", partition=" + melding.partition());
            sb.append(", payload=" + json);
            String msg = sb.toString();
            log.error("Forsøkte å konsumere Kafka-melding vedtak replikert for topic" + vedtakReplikertConfig.getTopic() + ", se securelog for detaljer");
            secureLog.error(msg, e);
            throw e;
        }
    }
}
