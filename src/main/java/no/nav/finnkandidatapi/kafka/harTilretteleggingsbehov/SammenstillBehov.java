package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
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
@Slf4j
public class SammenstillBehov {

    private final KandidatRepository kandidatRepository;
    private final PermittertArbeidssokerService permittertArbeidssokerService;
    private final VedtakService vedtakService;

    public SammenstillBehov(
            KandidatRepository kandidatRepository,
            PermittertArbeidssokerService permittertArbeidssokerService,
            VedtakService vedtakService) {
        this.kandidatRepository = kandidatRepository;
        this.permittertArbeidssokerService = permittertArbeidssokerService;
        this.vedtakService = vedtakService;
    }

    public HarTilretteleggingsbehov lagbehov(
            String aktørId,
            Optional<HarTilretteleggingsbehov> tilretteleggingsbehovInput,
            Optional<PermittertArbeidssoker> permittertArbeidssokerInput,
            Optional<Vedtak> vedtakInput
    ) {
        Optional<HarTilretteleggingsbehov> harTilretteleggingsbehov
                = tilretteleggingsbehovInput.or(() -> kandidatRepository.hentHarTilretteleggingsbehov(aktørId));
        Optional<PermittertArbeidssoker> permittertArbeidssoker
                = permittertArbeidssokerInput.or(() -> permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørId));
        Optional<Vedtak> vedtak
                = vedtakInput.or(() -> vedtakService.hentNyesteVedtakForAktør(aktørId));

        List<String> tilretteleggingsbehovFilter = harTilretteleggingsbehov.map(HarTilretteleggingsbehov::getBehov).orElse(emptyList());
        boolean erTilretteleggingsbehov = harTilretteleggingsbehov.filter(t -> t.isHarTilretteleggingsbehov()).isPresent();

        Optional<String> permitteringFilter = SjekkPermittertUtil.sjekkOmErPermittert(permittertArbeidssoker, vedtak)
                ? Optional.of(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI)
                : Optional.empty();


        var behov = concatToList(
                tilretteleggingsbehovFilter.stream(),
                permitteringFilter.stream()
        );
        log.info("Lager behov for aktør:" + aktørId + " " + behov);
        return new HarTilretteleggingsbehov(aktørId, erTilretteleggingsbehov, behov);
    }

    public HarTilretteleggingsbehov lagbehov(String aktørId) {
        return lagbehov(aktørId, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public HarTilretteleggingsbehov lagbehovKandidat(HarTilretteleggingsbehov harTilretteleggingsbehov) {
        return lagbehov(harTilretteleggingsbehov.getAktoerId(), Optional.of(harTilretteleggingsbehov), Optional.empty(), Optional.empty());

    }

    public HarTilretteleggingsbehov lagbehov(PermittertArbeidssoker permittertArbeidssoker) {
        return lagbehov(permittertArbeidssoker.getAktørId(), Optional.empty(), Optional.of(permittertArbeidssoker), Optional.empty());
    }

    public HarTilretteleggingsbehov lagbehov(Vedtak vedtak) {
        return lagbehov(vedtak.getAktørId(), Optional.empty(), Optional.empty(), Optional.of(vedtak));
    }

    private <T> List<T> concatToList(Stream<T>... s) {
        return Arrays.stream(s).reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }
}
