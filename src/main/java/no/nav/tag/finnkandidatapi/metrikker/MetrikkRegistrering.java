package no.nav.tag.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import no.nav.tag.finnkandidatapi.kandidat.Brukertype;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet event) {
        MetricsFactory.createEvent("finn-kandidat.kandidat.opprettet").report();
    }

    @EventListener
    public void kandidatEndret(KandidatEndret event) {
        MetricsFactory.createEvent("finn-kandidat.kandidat.endret").report();
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet event) {
        if (event.getSlettetAv().equals(Brukertype.VEILEDER)) {
            MetricsFactory.createEvent("finn-kandidat.kandidat.slettet")
                    .addTagToReport("slettetAv", Brukertype.VEILEDER.name())
                    .report();

        } else if (event.getSlettetAv().equals(Brukertype.SYSTEM)) {
            MetricsFactory.createEvent("finn-kandidat.kandidat.slettet")
                    .addTagToReport("slettetAv", Brukertype.SYSTEM.name())
                    .report();
        }
    }
}
