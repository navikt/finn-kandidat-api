package no.nav.finnkandidatapi.kafka.sisteOppfolgingsPeriode;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SisteOppfolgingsperiodeConsumer {

    private KandidatService kandidatService;
    private PermittertArbeidssokerService permittertArbeidssokerService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private OppfolgingsperiodeConfig oppfolgingsperiodeConfig;

    public SisteOppfolgingsperiodeConsumer(
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService,
            OppfolgingsperiodeConfig oppfolgingsperiodeConfig
    ) {
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.oppfolgingsperiodeConfig = oppfolgingsperiodeConfig;
    }

    @KafkaListener(
            topics = "#{oppfolgingsperiodeConfig.getTopic()}",
            groupId = "finn-kandidat-siste-oppfolgingsperiode",
            clientIdPrefix = "siste-oppfolgingsperiode",
            containerFactory = "aivenKafkaListenerContainerFactory"
    )
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        log.info(
                "Konsumerer en siste-oppfolgingsperiode melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            SisteOppfolgingsperiodeV1 sisteOppfolgingsperiode = SisteOppfolgingsperiodeUtils.deserialiserMelding(melding.value());
            if (sisteOppfolgingsperiode.getSluttDato() != null) {
                kandidatService.behandleOppfølgingAvsluttet(sisteOppfolgingsperiode);
                permittertArbeidssokerService.behandleOppfølgingAvsluttet(sisteOppfolgingsperiode);
            }

        } catch (RuntimeException e) {
            log.error("Feil ved konsumering av en siste-oppfolgingsperiode melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }
}
