package no.nav.finnkandidatapi.kafka.arbeidssøkerRegistrert;

import com.yammer.metrics.core.MetricsRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
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

    private static final String REGISTRERT_ARBEIDSSOKER_FEILET = "finnkandidat.registrertarbeidssoker.feilet";
    private static final String REGISTRERT_ARBEIDSSOKER_DROPPET = "finnkandidat.registrertarbeidssoker.droppet";
    private static final String REGISTRERT_ARBEIDSSOKER_AKSEPTERT = "finnkandidat.registrertarbeidssoker.akseptert";

    private final PermittertArbeidssokerService permittertArbeidssokerService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig;
    private final MeterRegistry meterRegistry;
    private final MetricsClient metricsClient;

    public ArbeidssokerRegistrertConsumer(
            PermittertArbeidssokerService permittertArbeidssokerService,
            ArbeidssokerRegistrertConfig arbeidssokerRegistrertConfig,
            MeterRegistry meterRegistry,
            MetricsClient metricsClient) {
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.arbeidssokerRegistrertConfig = arbeidssokerRegistrertConfig;
        this.meterRegistry = meterRegistry;
        this.metricsClient = metricsClient;
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

        Event event = new Event("finn-kandidat.permittertas.mottatt" )
                .addTagToReport("status", arbeidssokerRegistrert.getBrukersSituasjon());
        metricsClient.report(event);

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

        ArbeidssokerRegistrertDTO arbeidssokerRegistrertDTO = mapEventTilDto(arbeidssokerRegistrert);

        if (harBrukerRegistrertSegSomPermittert(arbeidssokerRegistrertDTO)) {
            permittertArbeidssokerService.behandleArbeidssokerRegistrert(arbeidssokerRegistrertDTO);
            meterRegistry.counter(REGISTRERT_ARBEIDSSOKER_AKSEPTERT).increment();
        } else {
            meterRegistry.counter(REGISTRERT_ARBEIDSSOKER_DROPPET).increment();
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
