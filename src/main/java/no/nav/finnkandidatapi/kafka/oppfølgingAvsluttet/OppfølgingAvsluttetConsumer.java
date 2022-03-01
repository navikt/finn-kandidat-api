package no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OppfølgingAvsluttetConsumer {

    private KandidatService kandidatService;
    private PermittertArbeidssokerService permittertArbeidssokerService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig;

    public OppfølgingAvsluttetConsumer(
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService,
            OppfolgingAvsluttetConfig oppfolgingAvsluttetConfig
    ) {
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.oppfolgingAvsluttetConfig = oppfolgingAvsluttetConfig;
    }

    @KafkaListener(
            topics = "#{oppfolgingAvsluttetConfig.getTopic()}",
            groupId = "finn-kandidat-oppfolging-avsluttet",
            clientIdPrefix = "oppfolging-avsluttet",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer avsluttet oppfølging melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            OppfølgingAvsluttetMelding oppfølgingAvsluttetMelding = OppfølgingAvsluttetUtils.deserialiserMelding(melding.value());
            permittertArbeidssokerService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding);
            kandidatService.behandleOppfølgingAvsluttet(oppfølgingAvsluttetMelding); // Rekkefølgen er viktig, permittering må være lagret før eventen her sendes ut

        } catch (RuntimeException e) {
            log.error("Feil ved konsumering av avsluttet oppfølging melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }
}
