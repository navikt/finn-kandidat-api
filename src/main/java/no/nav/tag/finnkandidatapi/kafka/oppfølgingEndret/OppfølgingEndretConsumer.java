package no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.KandidatService;
import no.nav.tag.finnkandidatapi.veilarbarena.Oppfølgingsbruker;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.tag.finnkandidatapi.kafka.oppfølgingEndret.OppfølgingEndretUtils.deserialiserMelding;

@Slf4j
@Component
public class OppfølgingEndretConsumer {

    private static final String ENDRET_OPPFØLGING_FEILET = "finnkandidat.endretoppfolging.feilet";

    private KandidatService kandidatService;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private ConsumerProps consumerProps;
    private MeterRegistry meterRegistry;

    public OppfølgingEndretConsumer(
            KandidatService kandidatService,
            ConsumerProps consumerProps,
            MeterRegistry meterRegistry
    ) {
        this.kandidatService = kandidatService;
        this.consumerProps = consumerProps;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(ENDRET_OPPFØLGING_FEILET);
    }

    @KafkaListener(topics = "#{consumerProps.getTopic()}", groupId = "finn-kandidat")
    public void konsumerMelding(ConsumerRecord<String, String> melding) {
        // TODO: Fjern logging av hver eneste konsumerte melding?
        log.info(
                "Konsumerer endret oppfølging-melding for id {}, offset: {}, partition: {}",
                melding.key(),
                melding.offset(),
                melding.partition()
        );

        try {
            Oppfølgingsbruker oppfølgingEndretMelding = deserialiserMelding(melding.value());
            kandidatService.behandleOppfølgingEndret(oppfølgingEndretMelding);

        } catch (RuntimeException e) {
            meterRegistry.counter(ENDRET_OPPFØLGING_FEILET).increment();
            log.error("Feil ved konsumering av endret oppfølging-melding. id {}, offset: {}, partition: {}",
                    melding.key(),
                    melding.offset(),
                    melding.partition()
            );
            throw e;
        }
    }
}
