package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.finnkandidatapi.permittert.ArbeidssokerRegistrertDTO;
import no.nav.finnkandidatapi.permittert.DinSituasjonSvarFraVeilarbReg;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.stereotype.Component;

import static no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert.VeilArbRegistreringOpprettetParser.parseTidspunkt;

@Slf4j
@Component
@Profile("!local" )
public class ArbeidssokerRegistrertConsumer {

    private PermittertArbeidssokerService permittertArbeidssokerService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig;

    public ArbeidssokerRegistrertConsumer(
            PermittertArbeidssokerService permittertArbeidssokerService,
            ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig
    ) {
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.arbeidssokerRegistrertConfig = arbeidssokerRegistrertConfig;
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
            log.error("Feil ved konsumering av registrert arbeidssøker-melding. id {}, offset: {}, partition: {}, årsak: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition(),
                    failedDeserializationInfo
            );
            throw new RuntimeException("Kunne ikke deserialisere ArbeidssokerRegistrertEvent", failedDeserializationInfo.getException());
        }

        ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO = mapEventTilDto(arbeidssokerRegistrert);

        if (harBrukerRegistrertSegSomPermittert(arbeidssokerRegistrertDTO)) {
            permittertArbeidssokerService.behandleArbeidssokerRegistrert(arbeidssokerRegistrertDTO);
        }
    }

    private boolean harBrukerRegistrertSegSomPermittert(ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO) {
        return arbeidssokerRegistrertDTO.getStatus().equalsIgnoreCase(DinSituasjonSvarFraVeilarbReg.ER_PERMITTERT.name());
    }

    private ArbeidssokerRegistrertDTO mapEventTilDto(ArbeidssokerRegistrertEvent arbeidssokerRegistrert) {
        return ArbeidssokerRegistrertDTO.builder()
                .aktørId(arbeidssokerRegistrert.getAktorid())
                .status(arbeidssokerRegistrert.getBrukersSituasjon())
                .registreringTidspunkt(parseTidspunkt(arbeidssokerRegistrert.getRegistreringOpprettet()))
                .build();
    }
}
