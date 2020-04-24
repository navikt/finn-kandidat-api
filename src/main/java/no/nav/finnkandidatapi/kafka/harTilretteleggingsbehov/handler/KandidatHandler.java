package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class KandidatHandler {
    private SammenstillBehov sammenstillBehov;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    public KandidatHandler(
            SammenstillBehov sammenstillBehov, HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer) {
        this.sammenstillBehov = sammenstillBehov;
        this.harTilretteleggingsbehovProducer = harTilretteleggingsbehovProducer;
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        Kandidat kandidat = event.getKandidat();
        kandidatOpprettetEllerEndret(kandidat);
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        Kandidat kandidat = event.getKandidat();
        kandidatOpprettetEllerEndret(kandidat);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        harTilretteleggingsbehovProducer.sendKafkamelding(
                sammenstillBehov.lagbehovKandidat(
                        new HarTilretteleggingsbehov(event.getAktørId(), false, Collections.emptyList()))
        );
    }

    private void kandidatOpprettetEllerEndret(Kandidat kandidat) {
        HarTilretteleggingsbehov behov = sammenstillBehov.lagbehovKandidat(
                new HarTilretteleggingsbehov(
                        kandidat.getAktørId(),
                        CollectionUtils.isNotEmpty(kandidat.kategorier()),
                        kandidat.kategorier()));
        harTilretteleggingsbehovProducer.sendKafkamelding(behov);
    }
}
