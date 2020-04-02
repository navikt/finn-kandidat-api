package no.nav.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.metrics.MetricsFactory;
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

    @EventListener
    public void permitteringMottattUtenKandidat(PermittertArbeidssokerEndretEllerOpprettet event) {
        MetricsFactory.createEvent("finn-kandidat.permittertas.lagret")
                .addTagToReport("status",event.getPermittertArbeidssoker().getStatusFraVeilarbRegistrering())
                .report();
    }
}
