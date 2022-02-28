package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakRepository;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SammenstillBehovTest {

    private SammenstillBehov sammenstillBehov;

    private String aktørid = "12333";

    @Mock
    public KandidatRepository kandidatRepository;

    @Mock
    public VedtakRepository vedtakRepository;

    @Mock
    public PermittertArbeidssokerService permittertArbeidssokerService;

    @Mock
    MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    @Before
    public void before() {
        sammenstillBehov = new SammenstillBehov(
                kandidatRepository,
                permittertArbeidssokerService,
                new VedtakService(vedtakRepository, null, null),
                midlertidigUtilgjengeligService
        );

        HarTilretteleggingsbehov harTilretteleggingsbehov =
                new HarTilretteleggingsbehov(aktørid, true, TestData.enKandidat().kategorier());
        PermittertArbeidssoker permittertArbeidssoker = TestData.enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørid);
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig(aktørid);

        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørid)).thenReturn(Optional.of(harTilretteleggingsbehov));
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.of(permittertArbeidssoker));
        when(vedtakRepository.hentNyesteVersjonAvNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
        when(vedtakRepository.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
        when(midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(aktørid)).thenReturn(Optional.of(midlertidigUtilgjengelig));
    }

    @Test
    public void lag_sammenstilling_av_alle_tabeller() {

        HarTilretteleggingsbehov lagbehov = sammenstillBehov.lagbehov(aktørid);

        assertThat(lagbehov.getAktoerId()).isEqualTo(aktørid);
        assertThat(lagbehov.isHarTilretteleggingsbehov()).isTrue();
        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_kandidatevent() {

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehovKandidat(
                        new HarTilretteleggingsbehov(aktørid, true, Collections.singletonList("t1"))
                );
        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "t1",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_vedtakevent() {

        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehov(
                        vedtak
                );

        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_ingen_event() {

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehov(aktørid);

        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_avsluttet_vedtak_i_db() {
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);
        Vedtak avsluttetVedtak = TestData.etAvsluttetVedtak();
        avsluttetVedtak.setAktørId(aktørid);
        when(vedtakRepository.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(avsluttetVedtak));
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.empty());

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehov(aktørid);

        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_aktivt_vedtak_i_db() {
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);
        when(vedtakRepository.hentNyesteVersjonAvNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
        when(vedtakRepository.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.empty());

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehov(aktørid);

        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG
                );
    }

    @Test
    public void lag_sammenstilling_med_midlertidig_utilgjengelig_event() {

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig(aktørid);
        midlertidigUtilgjengelig.setTilDato(LocalDateTime.now().plusDays(1));

        HarTilretteleggingsbehov lagbehov =
                sammenstillBehov.lagbehov(
                        midlertidigUtilgjengelig
                );

        assertThat(lagbehov.getBehov())
                .containsExactlyInAnyOrder(
                        "arbeidstid",
                        "fysisk",
                        "arbeidshverdagen",
                        "utfordringerMedNorsk",
                        PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                        MidlertidigUtilgjengelig.TILGJENGELIG_INNEN_1_UKE
                );
    }

}