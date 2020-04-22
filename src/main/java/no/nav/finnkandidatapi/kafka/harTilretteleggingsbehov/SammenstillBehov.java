package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Component
public class SammenstillBehov {

    private final KandidatRepository kandidatRepository;
    private final PermittertArbeidssokerService permittertArbeidssokerService;
    private final VedtakService vedtakService;
    private final MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    public SammenstillBehov(
            KandidatRepository kandidatRepository,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService,
            MidlertidigUtilgjengeligService midlertidigUtilgjengeligService) {
        this.kandidatRepository = kandidatRepository;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
        this.midlertidigUtilgjengeligService = midlertidigUtilgjengeligService;
    }

    public HarTilretteleggingsbehov lagbehov(
            String aktørId,
            Optional<HarTilretteleggingsbehov> tilretteleggingsbehovInput,
            Optional<PermittertArbeidssoker> permittertArbeidssokerInput,
            Optional<Vedtak> vedtakInput,
            Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengeligInput
    ) {
        Optional<HarTilretteleggingsbehov> harTilretteleggingsbehov
                = tilretteleggingsbehovInput.or(() -> kandidatRepository.hentHarTilretteleggingsbehov(aktørId));
        Optional<PermittertArbeidssoker> permittertArbeidssoker
                = permittertArbeidssokerInput.or(() -> permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørId));
        Optional<Vedtak> vedtak
                = vedtakInput.or(() -> vedtakService.hentNyesteVedtakForAktør(aktørId));
        Optional<MidlertidigUtilgjengelig> midlertidigUtilgjengelig
                = midlertidigUtilgjengeligInput.or(() -> midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(aktørId));

        List<String> tilretteleggingsbehovFilter = harTilretteleggingsbehov.map(HarTilretteleggingsbehov::getBehov).orElse(emptyList());
        boolean erTilretteleggingsbehov = harTilretteleggingsbehov.filter(t -> t.isHarTilretteleggingsbehov() == true).isPresent();

        Optional<String> permitteringFilter = SjekkPermittertUtil.sjekkOmErPermittert(permittertArbeidssoker, vedtak)
                ? Optional.of(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI)
                : Optional.empty();

        Optional<String> midlertidigUtilgjengeligFilter = MidlertidigUtilgjengelig.finnMidlertidigUtilgjengeligFilter(midlertidigUtilgjengelig);

        var behov = concatToList(
                tilretteleggingsbehovFilter.stream(),
                permitteringFilter.stream(),
                midlertidigUtilgjengeligFilter.stream()
        );

        return new HarTilretteleggingsbehov(aktørId, erTilretteleggingsbehov, behov);
    }

    public HarTilretteleggingsbehov lagbehov(String aktørId) {
        return lagbehov(aktørId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public HarTilretteleggingsbehov lagbehovKandidat(HarTilretteleggingsbehov harTilretteleggingsbehov) {
        return lagbehov(harTilretteleggingsbehov.getAktoerId(), Optional.of(harTilretteleggingsbehov), Optional.empty(), Optional.empty(), Optional.empty());

    }

    public HarTilretteleggingsbehov lagbehov(PermittertArbeidssoker permittertArbeidssoker) {
        return lagbehov(permittertArbeidssoker.getAktørId(), Optional.empty(), Optional.of(permittertArbeidssoker), Optional.empty(), Optional.empty());
    }

    public HarTilretteleggingsbehov lagbehov(Vedtak vedtak) {
        return lagbehov(vedtak.getAktørId(), Optional.empty(), Optional.empty(), Optional.of(vedtak), Optional.empty());
    }

    public HarTilretteleggingsbehov lagbehov(MidlertidigUtilgjengelig midlertidigUtilgjengelig) {
        return lagbehov(midlertidigUtilgjengelig.getAktørId(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(midlertidigUtilgjengelig));
    }

    private <T> List<T> concatToList(Stream<T>... s) {
        return Arrays.stream(s).reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }
}
