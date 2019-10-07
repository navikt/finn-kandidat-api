package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
@Slf4j
public class  KandidatEndretProducer {

    private static final String KANDIDAT_ENDRET_PRODUSENT_FEILET = "finnkandidat.kandidatendret.feilet";
    private static final String KANDIDAT_ENDRET_PRODUSENT_SUKSESS = "finnkandidat.kandidatendret.suksess";

    private KafkaTemplate<String, String> kafkaTemplate;
    private String topic;
    private MeterRegistry meterRegistry;

    public KandidatEndretProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kandidat-endret.topic}") String topic,
            MeterRegistry meterRegistry
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET);
        meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_SUKSESS);
    }

    public void kandidatEndret(String aktørId, Boolean harTilretteleggingsbehov) {
        try {
            KandidatEndret kandidatEndret = new KandidatEndret(aktørId, harTilretteleggingsbehov);
            ObjectMapper mapper = new ObjectMapper();
            String serialisertKandidatEndret = mapper.writeValueAsString(kandidatEndret);

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    topic,
                    aktørId,
                    serialisertKandidatEndret
            );

            future.addCallback(result -> {
                log.info("Kandidats behov for tilrettelegging sendt på Kafka-topic, aktørId: {}", aktørId);
                meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_SUKSESS).increment();
            },
            exception -> {
                log.error("Kunne ikke sende kandidat på Kafka-topic, aktørId: {}", aktørId, exception);
                meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET).increment();
            });

        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere kandidat endret", e);
            meterRegistry.counter(KANDIDAT_ENDRET_PRODUSENT_FEILET).increment();
        }
    }
}
