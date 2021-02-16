package no.nav.finnkandidatapi.metrikker;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakEndret;
import no.nav.finnkandidatapi.vedtak.VedtakOpprettet;
import no.nav.finnkandidatapi.vedtak.VedtakSlettet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {

    private final MetricsClient metricsClient;

    public MetrikkRegistrering(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
    }

    @EventListener
    public void kandidatOpprettet(KandidatOpprettet kandidatOpprettet) {
        metricsClient.report(new Event("finn-kandidat.kandidat.opprettet"));
    }

    @EventListener
    public void kandidatEndret(KandidatEndret kandidatEndret) {
        metricsClient.report(new Event("finn-kandidat.kandidat.endret"));
    }

    @EventListener
    public void vedtakOpprettet(VedtakOpprettet vedtakOpprettet) {
        rapporterVedtakEvent(vedtakOpprettet.getVedtak());
    }

    @EventListener
    public void vedtakEndret(VedtakEndret vedtakEndret) {
        rapporterVedtakEvent(vedtakEndret.getVedtak());
    }

    @EventListener
    public void vedtakSlettet(VedtakSlettet vedtakSlettet) {
        rapporterVedtakEvent(vedtakSlettet.getVedtak());
    }

    private void rapporterVedtakEvent(Vedtak vedtak) {
        Event event = new Event("finn-kandidat.vedtak.lagret" )
                .addTagToReport("operasjon", vedtak.getArenaDbOperasjon())
                .addTagToReport("rettighet", vedtak.getRettighetKode())
                .addTagToReport("status", vedtak.getStatusKode())
                .addTagToReport("type", vedtak.getTypeKode())
                .addTagToReport("utfall", vedtak.getUtfallKode());
        metricsClient.report(event);
    }

    @EventListener
    public void kandidatSlettet(KandidatSlettet kandidatSlettet) {
        Event event = new Event("finn-kandidat.kandidat.slettet")
                .addTagToReport("slettetAv", kandidatSlettet.getSlettetAv().name());
        metricsClient.report(event);
    }

    @EventListener
    public void permitteringLagret(PermittertArbeidssokerEndretEllerOpprettet permittertArbeidssokerEndretEllerOpprettet) {
        Event event = new Event("finn-kandidat.permittertas.lagret")
                .addTagToReport("status", permittertArbeidssokerEndretEllerOpprettet.getPermittertArbeidssoker().getStatusFraVeilarbRegistrering());
        metricsClient.report(event);
    }
}
