package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class HarTilretteleggingsbehovProducer {

    private static final String KANDIDAT_ENDRET_PRODUSENT_FEILET = "finnkandidat.kandidatendret.feilet";

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private MeterRegistry meterRegistry;

    public HarTilretteleggingsbehovProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}") String topic,
            MeterRegistry meterRegistry
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET);
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        sendKafkamelding(event.getKandidat().getAktørId(), true);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        sendKafkamelding(event.getAktørId(), false);
    }

    public void sendKafkamelding(String aktørId, Boolean harTilretteleggingsbehov) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HarTilretteleggingsbehov melding = new HarTilretteleggingsbehov(aktørId, harTilretteleggingsbehov);
            String serialisertMelding = mapper.writeValueAsString(melding);

            SendResult<String, String> result = kafkaTemplate.send(
                    topic,
                    aktørId,
                    serialisertMelding
            ).get();

            // TODO: Logge mer her? Ok å logge aktørId?
            log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic, offset: {}",
                    result.getRecordMetadata().offset());

            // sjekk antall meldinger med inkludering prometheus melding

        } catch (JsonProcessingException e) {
            // TODO: Ha varsel på dette i Grafana med all info som trengs
            meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET).increment();
            log.error("Kunne ikke serialisere kandidat endret", e);

        } catch (InterruptedException | ExecutionException e) {
            // TODO: Ha varsel på dette i Grafana med all info som trengs
            meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET).increment();
            // TOOD: Håndter kafka-meldinger som ikke ble sendt.
            log.error("Kunne ikke sende kandidat på Kafka-topic", e);
        }
    }
}
