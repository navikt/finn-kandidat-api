package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KandidatServiceTest {

    private KandidatService kandidatService;

    @Mock
    private KandidatRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DateProvider dateProvider;

    @Before
    public void setUp() {
        kandidatService = new KandidatService(repository, eventPublisher, dateProvider);
    }

    @Test
    public void hentNyesteKandidat__skal_returnere_kandidat() {
        Kandidat kandidat = enKandidat();
        when(repository.hentNyesteKandidat(kandidat.getFnr())).thenReturn(Optional.of(kandidat));

        Kandidat hentetKandidat = kandidatService.hentNyesteKandidat(kandidat.getFnr()).get();

        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void hentKandidater__skal_returnere_kandidater() {
        Kandidat kandidat1 = enKandidat("1234567890");
        Kandidat kandidat2 = enKandidat("2345678901");
        when(repository.hentKandidater()).thenReturn(List.of(kandidat1, kandidat2));

        List<Kandidat> hentedeKandidater = kandidatService.hentKandidater();

        assertThat(hentedeKandidater).containsExactly(kandidat1, kandidat2);
    }

    @Test
    public void opprettKandidat__skal_endre_sistEndretAv_og_sistEndret_med_innlogget_veileder() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();

        when(dateProvider.now()).thenReturn(datetime);
        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.opprettKandidat(kandidat, veileder);

        assertThat(kandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
        assertThat(kandidat.getSistEndret()).isEqualTo(datetime);
    }

    @Test
    public void opprettKandidat__skal_publisere_KandidatOpprettet_event() {
        Kandidat kandidat = enKandidat();
        when(repository.lagreKandidat(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.opprettKandidat(kandidat, enVeileder());

        verify(eventPublisher).publishEvent(new KandidatOpprettet(kandidat));
    }

    @Test
    public void endreKandidat__skal_endre_sistEndretAv_og_sistEndret_med_innlogget_veileder() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();

        when(dateProvider.now()).thenReturn(datetime);
        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.endreKandidat(kandidat, veileder);

        assertThat(kandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
        assertThat(kandidat.getSistEndret()).isEqualTo(datetime);
    }

    @Test
    public void endreKandidat__skal_publisere_KandidatEndret_event() {
        Kandidat kandidat = enKandidat();
        when(repository.lagreKandidat(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.endreKandidat(kandidat, enVeileder());

        verify(eventPublisher).publishEvent(new KandidatEndret(kandidat));
    }

    @Test
    public void slettKandidat_skal_slette_kandidat_med_riktig_fnr_veilder_og_tidspunkt() {
        String fnr = "12345678910";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();
        when(dateProvider.now()).thenReturn(datetime);

        kandidatService.slettKandidat(fnr, veileder);

        verify(repository).slettKandidat(fnr, veileder, datetime);
    }

    @Test
    public void slettKandidat_skal_publisere_KandidatSlettet_event() {
        String fnr = "12345678910";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();

        when(dateProvider.now()).thenReturn(datetime);
        when(repository.slettKandidat(fnr, veileder, datetime)).thenReturn(Optional.of(4));

        kandidatService.slettKandidat(fnr, veileder);

        verify(eventPublisher).publishEvent(new KandidatSlettet(4, fnr, veileder, datetime));
    }

    @Test
    public void slettKandidat_skal_returnere_id() {
        String fnr = "12345678910";
        Veileder veileder = enVeileder();

        when(repository.slettKandidat(eq(fnr), eq(veileder), any())).thenReturn(Optional.of(4));

        assertThat(kandidatService.slettKandidat(fnr, veileder).get()).isEqualTo(4);
    }
}
