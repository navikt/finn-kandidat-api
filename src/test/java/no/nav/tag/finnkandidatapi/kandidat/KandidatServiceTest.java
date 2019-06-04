package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KandidatServiceTest {

    @InjectMocks
    private KandidatService kandidatService;

    @Mock
    private KandidatRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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

        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.opprettKandidat(kandidat, veileder);

        assertThat(kandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
        assertThat(kandidat.getSistEndret()).isEqualToIgnoringSeconds(LocalDateTime.now());
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

        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        kandidatService.endreKandidat(kandidat, veileder);

        assertThat(kandidat.getSistEndretAv()).isEqualTo(veileder.getNavIdent());
        assertThat(kandidat.getSistEndret()).isEqualToIgnoringSeconds(LocalDateTime.now());
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
    public void slettKandidat__skal_returnere_antall_slettede_kandidater() {
        String fnr = enKandidat("12345678901").getFnr();

        when(repository.slettKandidat(fnr)).thenReturn(1);

        assertThat(kandidatService.slettKandidat(fnr)).isEqualTo(1);
    }

    @Test
    public void markerKandidatSomSlettet__skal_returnere_antall_kandidater_markert_som_slettet() {
        String fnr = enKandidat("12345678901").getFnr();

        when(repository.markerKandidatSomSlettet(fnr)).thenReturn(1);
        assertThat(kandidatService.markerKandidatSomSlettet(fnr)).isEqualTo(1);
    }
}
