package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import org.junit.Before;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Date;
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

    @Mock
    private AktørRegisterClient aktørRegisterClient;

    @Before
    public void setUp() {
        kandidatService = new KandidatService(repository, eventPublisher, aktørRegisterClient, dateProvider);
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
        Kandidat kandidat1 = enKandidat("1000000000001");
        Kandidat kandidat2 = enKandidat("1000000000002");
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
    public void slettKandidat_skal_slette_kandidat_med_riktig_aktor_id_veilder_og_tidspunkt() {
        String aktorId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();
        when(dateProvider.now()).thenReturn(datetime);

        kandidatService.slettKandidat(aktorId, veileder);

        verify(repository).slettKandidat(aktorId, veileder, datetime);
    }

    @Test
    public void slettKandidat_skal_publisere_KandidatSlettet_event() {
        String aktorId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();

        when(dateProvider.now()).thenReturn(datetime);
        when(repository.slettKandidat(aktorId, veileder, datetime)).thenReturn(Optional.of(4));

        kandidatService.slettKandidat(aktorId, veileder);

        verify(eventPublisher).publishEvent(new KandidatSlettet(4, aktorId, veileder, datetime));
    }

    @Test
    public void slettKandidat_skal_returnere_id() {
        String aktorId = "1000000000001";
        Veileder veileder = enVeileder();

        when(repository.slettKandidat(eq(aktorId), eq(veileder), any())).thenReturn(Optional.of(4));

        assertThat(kandidatService.slettKandidat(aktorId, veileder).get()).isEqualTo(4);
    }

    @Test
    public void behandleOppfølgingAvsluttet__skal_slette_kandidat() {
        String aktorId = "1000000000001";

        kandidatService.behandleOppfølgingAvsluttet(new OppfølgingAvsluttetMelding(aktorId, new Date()));

        verify(repository).slettKandidatSomMaskinbruker(aktorId, dateProvider.now());
    }
}
