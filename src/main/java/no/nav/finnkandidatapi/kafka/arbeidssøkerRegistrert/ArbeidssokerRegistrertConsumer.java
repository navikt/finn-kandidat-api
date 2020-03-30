package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local" )
public class ArbeidssokerRegistrertConsumer {

    private static final String REGISTRERT_ARBEIDSSOKER_FEILET = "finnkandidat.registrertarbeidssoker.feilet";

    private KandidatService kandidatService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig;
    private MeterRegistry meterRegistry;

    public ArbeidssokerRegistrertConsumer(
            KandidatService kandidatService,
            ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig,
            MeterRegistry meterRegistry
    ) {
        this.kandidatService = kandidatService;
        this.arbeidssokerRegistrertConfig = arbeidssokerRegistrertConfig;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(REGISTRERT_ARBEIDSSOKER_FEILET);
    }

    @KafkaListener(
            topics = "#{arbeidssokerRegistrertConfig.getTopic()}",
            groupId = "finn-kandidat-arbeidssoker-registrert",
            clientIdPrefix = "arbeidssoker-registrert",
            containerFactory = "avroKafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, ArbeidssokerRegistrertEvent> melding) {
        log.info(
                "Konsumerer registrert Arbeidssøker-melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        ArbeidssokerRegistrertEvent arbeidssokerRegistrert = melding.value();

        if (arbeidssokerRegistrert instanceof FaultyArbeidssokerRegistrert) {
            FailedDeserializationInfo failedDeserializationInfo = ((FaultyArbeidssokerRegistrert) arbeidssokerRegistrert).getFailedDeserializationInfo();
            meterRegistry.counter(REGISTRERT_ARBEIDSSOKER_FEILET).increment();
            log.error("Feil ved konsumering av registrert arbeidssøker-melding. id {}, offset: {}, partition: {}, årsak: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition(),
                    failedDeserializationInfo
            );
            throw new RuntimeException("Kunne ikke deserialisere ArbeidssokerRegistrertEvent", failedDeserializationInfo.getException());
        }
        kandidatService.behandleArbeidssøkerRegistrert(arbeidssokerRegistrert);
    }
}
