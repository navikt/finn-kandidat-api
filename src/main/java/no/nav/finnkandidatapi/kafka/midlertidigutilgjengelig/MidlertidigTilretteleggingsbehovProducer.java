package no.nav.finnkandidatapi.kafka.midlertidigutilgjengelig;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MidlertidigTilretteleggingsbehovProducer {

    public static final String MIDLERTIDIG_UTILGJENGELIG_1_UKE = "MIDLERTIDIG_UTILGJENGELIG_1_UKE";
    public static final String MIDLERTIDIG_UTILGJENGELIG = "MIDLERTIDIG_UTILGJENGELIG";

    private KandidatService kandidatService;
    private PermittertArbeidssokerService permittertArbeidssokerService;
    private VedtakService vedtakService;
    private MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;
    private HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    public MidlertidigTilretteleggingsbehovProducer(
            KandidatService kandidatService,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService,
            MidlertidigUtilgjengeligService midlertidigUtilgjengeligService,
            HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer) {
        this.kandidatService = kandidatService;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
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
        Optional<PermittertArbeidssoker> permittertArbeidssoker = permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(midlertidigUtilgjengelig.getAktørId());
        Optional<Kandidat> kandidat = kandidatService.hentNyesteKandidat(midlertidigUtilgjengelig.getAktørId());
        Optional<Vedtak> vedtak = vedtakService.hentNyesteVedtakForAktør(midlertidigUtilgjengelig.getAktørId());
        List<String> kategorier = kandidat.map(Kandidat::kategorier).orElse(Collections.emptyList());

        harTilretteleggingsbehovProducer.lagOgSendMelding(midlertidigUtilgjengelig.getAktørId(), kategorier, permittertArbeidssoker, vedtak, Optional.of(midlertidigUtilgjengelig));
    }

    public static Optional<String> finnMidlertidigUtilgjengeligFilter(Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig) {
        if(midlertidigUtilgjengelig.isEmpty() ) {
            return Optional.empty();
        }
        LocalDateTime tilDato = midlertidigUtilgjengelig.get().getTilDato();
        LocalDateTime nå = LocalDateTime.now();
        if(tilDato == null) {
            return Optional.empty();
        } else if(tilDato.isBefore(nå)) {
            return Optional.empty();
        } else if(tilDato.isBefore(nå.plusWeeks(1))) {
            return Optional.of(MIDLERTIDIG_UTILGJENGELIG_1_UKE);
        } else {
            return Optional.of(MIDLERTIDIG_UTILGJENGELIG);
        }
    }
}
