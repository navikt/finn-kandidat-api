package no.nav.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakEndret;
import no.nav.finnkandidatapi.vedtak.VedtakOpprettet;
import no.nav.finnkandidatapi.vedtak.VedtakSlettet;
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
    public void vedtakOpprettet(VedtakOpprettet event) {
        rapporterVedtakEvent(event.getVedtak());
    }

    @EventListener
    public void vedtakEndret(VedtakEndret event) {
        rapporterVedtakEvent(event.getVedtak());
    }

    @EventListener
    public void vedtakSlettet(VedtakSlettet event) {
        rapporterVedtakEvent(event.getVedtak());
    }

    private void rapporterVedtakEvent(Vedtak vedtak) {
        MetricsFactory.createEvent("finn-kandidat.vedtak.lagret" )
                .addTagToReport("operasjon", vedtak.getArenaDbOperasjon())
                .addTagToReport("rettighet", vedtak.getRettighetKode())
                .addTagToReport("status", vedtak.getStatusKode())
                .addTagToReport("type", vedtak.getTypeKode())
                .addTagToReport("utfall", vedtak.getUtfallKode())
                .report();
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
    public void permitteringLagret(PermittertArbeidssokerEndretEllerOpprettet event) {
        MetricsFactory.createEvent("finn-kandidat.permittertas.lagret")
                .addTagToReport("status",event.getPermittertArbeidssoker().getStatusFraVeilarbRegistrering())
                .report();
    }
}
