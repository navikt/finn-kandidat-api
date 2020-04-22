package no.nav.finnkandidatapi.kafka.midlertidigutilgjengelig;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
public class MidlertidigTilretteleggingsbehovProducer {

    public static final String MIDLERTIDIG_UTILGJENGELIG_1_UKE = "MIDLERTIDIG_UTILGJENGELIG_1_UKE";
    public static final String MIDLERTIDIG_UTILGJENGELIG = "MIDLERTIDIG_UTILGJENGELIG";

    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    public MidlertidigTilretteleggingsbehovProducer(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
    }

    @EventListener
    public void midlertidigUtilgjengeligOpprettet(MidlertidigUtilgjengeligOpprettet midlertidigUtilgjengeligOpprettet) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligOpprettet.getMidlertidigUtilgjengelig());
    }

    @EventListener
    public void midlertidigUtilgjengeligEndret(MidlertidigUtilgjengeligEndret midlertidigUtilgjengeligEndret) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligEndret.getMidlertidigUtilgjengelig());
    }

    @EventListener
    public void midlertidigUtilgjengeligSlettet(MidlertidigUtilgjengeligSlettet midlertidigUtilgjengeligSlettet) {
        mottattMidlertidigUtilgjengeligEvent(midlertidigUtilgjengeligSlettet.getMidlertidigUtilgjengelig());
    }

    private void mottattMidlertidigUtilgjengeligEvent(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {
        harTilretteleggingsbehovProducer.sendKafkamelding(
                sammenstillBehov.lagbehov(midlertidigUtilgjengelig)
        );
    }

    public static Optional<String> finnMidlertidigUtilgjengeligFilter(Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig) {
        if (midlertidigUtilgjengelig.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime tilDato = midlertidigUtilgjengelig.get().getTilDato();
        LocalDateTime nå = LocalDateTime.now();
        if (tilDato == null) {
            return Optional.empty();
        } else if (tilDato.isBefore(nå)) {
            return Optional.empty();
        } else if (tilDato.isBefore(nå.plusWeeks(1))) {
            return Optional.of(MIDLERTIDIG_UTILGJENGELIG_1_UKE);
        } else {
            return Optional.of(MIDLERTIDIG_UTILGJENGELIG);
        }
    }
}
