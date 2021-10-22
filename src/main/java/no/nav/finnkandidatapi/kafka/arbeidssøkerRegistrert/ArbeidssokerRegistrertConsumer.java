package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.event.ConsumerStoppedEvent;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert.VeilArbRegistreringOpprettetParser.parseTidspunkt;
import static no.nav.finnkandidatapi.permittert.DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT;

@Slf4j
@Component
@Profile("!local")
public class ArbeidssokerRegistrertConsumer implements ApplicationContextAware {
    private final String topicName = "paw.arbeidssoker-registrert-v1";

    private final PermittertArbeidssokerService permittertArbeidssokerService;

    private ApplicationContext appCtxt;

    public ArbeidssokerRegistrertConsumer(PermittertArbeidssokerService permittertArbeidssokerService) {
        this.permittertArbeidssokerService = permittertArbeidssokerService;
    }

    @KafkaListener(
            topics = topicName,
            groupId = "finn-kandidat-arbeidssoker-registrert",
            clientIdPrefix = "arbeidssoker-registrert",
            containerFactory = "avroAivenKafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, ArbeidssokerRegistrertEvent> melding) {
        log.info(
                "Konsumerer registrert Arbeidssøker-melding fra topic {}, for id {}, offset: {}, partition: {}",
                topicName,
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        ArbeidssokerRegistrertEvent arbeidssokerRegistrert = melding.value();

        if (arbeidssokerRegistrert instanceof FaultyArbeidssokerRegistrert) {
            FailedDeserializationInfo failedDeserializationInfo = ((FaultyArbeidssokerRegistrert) arbeidssokerRegistrert).getFailedDeserializationInfo();
            log.error("Feil ved konsumering av registrert arbeidssøker-melding. Topic {}, id {}, offset: {}, partition: {}, årsak: {}",
                    topicName,
                    melding.key(),
                    melding.offset(),
                    melding.partition(),
                    failedDeserializationInfo
            );
            throw new RuntimeException("Kunne ikke deserialisere ArbeidssokerRegistrertEvent", failedDeserializationInfo.getException());
        }

        ArbeidssokerRegistrertDTO dto = mapEventTilDto(arbeidssokerRegistrert);

        if (brukerHarRegistrertSegSomPermittert(dto)) {
            permittertArbeidssokerService.behandleArbeidssokerRegistrert(dto);
        }
    }

    @EventListener
    public void eventHandler(ConsumerStoppedEvent event) {
        log.warn("En Kafka-konsument har stoppet. Stopper hele applikasjonen. " + event);
        ((ConfigurableApplicationContext) appCtxt).close();
    }

    private boolean brukerHarRegistrertSegSomPermittert(ArbeidssokerRegistrertDTO dto) {
        return dto.getStatus().equalsIgnoreCase(ER_PERMITTERT.name());
    }

    private ArbeidssokerRegistrertDTO mapEventTilDto(ArbeidssokerRegistrertEvent arbeidssokerRegistrert) {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId(arbeidssokerRegistrert.getAktorid())
                .status(arbeidssokerRegistrert.getBrukersSituasjon())
                .registreringTidspunkt(parseTidspunkt(arbeidssokerRegistrert.getRegistreringOpprettet()))
                .build();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        appCtxt = applicationContext;
    }
}
