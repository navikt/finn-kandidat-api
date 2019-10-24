package no.nav.tag.finnkandidatapi.kandidat;

import io.micrometer.core.instrument.MeterRegistry;
import no.finn.unleash.Unleash;
import no.nav.tag.finnkandidatapi.DateProvider;
import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.tag.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.tag.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.tag.finnkandidatapi.veilarbarena.VeilarbArenaClient;
import org.junit.Before;
import no.nav.tag.finnkandidatapi.metrikker.KandidatSlettet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.*;
import static no.nav.tag.finnkandidatapi.unleash.UnleashConfiguration.HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT;
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

    @Mock
    private VeilarbArenaClient veilarbArenaClient;

    @Mock
    private Unleash unleash;

    @Mock
    private MeterRegistry meterRegistry;

    @Before
    public void setUp() {
        when(unleash.isEnabled(HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT)).thenReturn(true);
        kandidatService = new KandidatService(
                repository,
                eventPublisher,
                aktørRegisterClient,
                dateProvider,
                veilarbArenaClient,
                unleash,
                meterRegistry
        );
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
    public void opprettKandidat__skal_opprette_kandidat_med_riktige_felter() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        kandidat.setSistEndretAv(veileder.getNavIdent());

        Kandidat kandidatTilOpprettelse = Kandidat.opprettKandidat(
                kandidat.getFnr(),
                kandidatDto,
                veileder,
                kandidat.getSistEndret(),
                etNavKontor()
        );

        when(dateProvider.now()).thenReturn(kandidat.getSistEndret());
        when(veilarbArenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId())).thenReturn(enOppfølgingsbruker());
        when(repository.lagreKandidat(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidatTilOpprettelse));

        Kandidat opprettetKandidat = kandidatService.opprettKandidat(kandidat.getFnr(), kandidatDto, veileder).get();

        verify(repository).lagreKandidat(opprettetKandidat);
    }

    @Test
    public void opprettKandidat__skal_returnere_empty_hvis_kandidat_ikke_ble_lagret() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        kandidat.setSistEndretAv(veileder.getNavIdent());

        when(dateProvider.now()).thenReturn(kandidat.getSistEndret());
        when(veilarbArenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId())).thenReturn(enOppfølgingsbruker());
        when(repository.lagreKandidat(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.empty());

        Optional<Kandidat> empty = kandidatService.opprettKandidat(kandidat.getFnr(), kandidatDto, veileder);

        assertThat(empty).isEmpty();
    }

    @Test
    public void opprettKandidat__skal_publisere_KandidatOpprettet_event() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Integer kandidatId = 1;

        when(dateProvider.now()).thenReturn(kandidat.getSistEndret());
        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(kandidatId);
        when(repository.hentKandidat(kandidatId)).thenReturn(Optional.of(kandidat));
        when(veilarbArenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId())).thenReturn(enOppfølgingsbruker());

        kandidatService.opprettKandidat(kandidat.getFnr(), kandidatDto, enVeileder());
        verify(eventPublisher).publishEvent(new KandidatOpprettet(kandidat));
    }

    @Test
    public void endreKandidat__skal_endre_kandidatens_felter() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();
        when(dateProvider.now()).thenReturn(datetime);
        Kandidat kandidatTilLagring = Kandidat.endreKandidat(kandidat, kandidatDto, veileder, dateProvider.now());

        when(repository.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));
        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidatTilLagring));

        Kandidat endretKandidat = kandidatService.endreKandidat(kandidatDto, veileder).get();

        verify(repository).lagreKandidat(endretKandidat);
    }

    @Test
    public void endreKandidat__skal_returnere_empty_hvis_ingen_kandidat() {
        KandidatDto kandidatDto = enKandidatDto();
        when(repository.hentNyesteKandidat(kandidatDto.getAktørId())).thenReturn(Optional.empty());
        Optional<Kandidat> endretKandidat = kandidatService.endreKandidat(kandidatDto, enVeileder());
        assertThat(endretKandidat).isEmpty();
    }

    @Test
    public void endreKandidat__skal_returnere_empty_hvis_man_ikke_kunne_lagre_kandidat() {
        KandidatDto kandidatDto = enKandidatDto();
        when(repository.hentNyesteKandidat(kandidatDto.getAktørId())).thenReturn(Optional.empty());
        Optional<Kandidat> endretKandidat = kandidatService.endreKandidat(enKandidatDto(), enVeileder());
        assertThat(endretKandidat).isEmpty();
    }

    @Test
    public void endreKandidat__skal_publisere_Kandidatoppdatering_event() {
        Kandidat kandidat = enKandidat();
        when(repository.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));
        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        Kandidat endretKandidat = kandidatService.endreKandidat(enKandidatDto(), enVeileder()).get();

        verify(eventPublisher).publishEvent(new KandidatEndret(endretKandidat));
    }

    @Test
    public void slettKandidat_skal_slette_kandidat_med_riktig_aktør_id_veilder_og_tidspunkt() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();
        when(dateProvider.now()).thenReturn(datetime);

        kandidatService.slettKandidat(aktørId, veileder);

        verify(repository).slettKandidat(aktørId, veileder, datetime);
    }

    @Test
    public void slettKandidat_skal_publisere_KandidatSlettet_event() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = LocalDateTime.now();

        when(dateProvider.now()).thenReturn(datetime);
        Optional<Integer> slettetKey = Optional.of(4);
        when(repository.slettKandidat(aktørId, veileder, datetime)).thenReturn(slettetKey);

        kandidatService.slettKandidat(aktørId, veileder);

        verify(eventPublisher).publishEvent(new KandidatSlettet(slettetKey.get(), aktørId, Brukertype.VEILEDER, datetime));
    }

    @Test
    public void slettKandidat_skal_returnere_id() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();

        when(repository.slettKandidat(eq(aktørId), eq(veileder), any())).thenReturn(Optional.of(4));

        assertThat(kandidatService.slettKandidat(aktørId, veileder).get()).isEqualTo(4);
    }

    @Test
    public void behandleOppfølgingAvsluttet__skal_slette_kandidat() {
        String aktørId = "1000000000001";

        kandidatService.behandleOppfølgingAvsluttet(new OppfølgingAvsluttetMelding(aktørId, new Date()));

        verify(repository).slettKandidatSomMaskinbruker(aktørId, dateProvider.now());
    }

    @Test
    public void behandleOppfølgingAvsluttet__skal_publisere_KandidatSlettet_event() {
        String aktørId = "1856024171652";
        LocalDateTime datetime = LocalDateTime.now();
        when(dateProvider.now()).thenReturn(datetime);
        Optional<Integer> slettetKey = Optional.of(4);
        when(repository.slettKandidatSomMaskinbruker(aktørId, datetime)).thenReturn(slettetKey);

        kandidatService.behandleOppfølgingAvsluttet(new OppfølgingAvsluttetMelding(aktørId, new Date()));

        verify(eventPublisher).publishEvent(new KandidatSlettet(slettetKey.get(), aktørId, Brukertype.SYSTEM, datetime));
    }

    @Test
    public void hentAktørId__skal_returnere_aktørId() {
        String fnr = "12345678901";
        String aktørId = "1856024171652";
        when(aktørRegisterClient.tilAktørId(fnr)).thenReturn(aktørId);

        String hentetAktørId = kandidatService.hentAktørId(fnr);
        assertThat(hentetAktørId).isEqualTo(aktørId);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentAktørId__skal_kaste_exception_hvis_aktørregister_ikke_finner_aktørId() {
        String fnr = "12345678901";
        when(aktørRegisterClient.tilAktørId(fnr)).thenThrow(FinnKandidatException.class);
        kandidatService.hentAktørId(fnr);
    }

    @Test
    public void hentFnr__skal_returnere_fnr() {
        String aktørId = "1856024171652";
        String fnr = "12345678901";
        when(aktørRegisterClient.tilFnr(aktørId)).thenReturn(fnr);

        String hentetFnr = kandidatService.hentFnr(aktørId);
        assertThat(hentetFnr).isEqualTo(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentFnr__skal_kaste_exception_hvis_aktørregister_ikke_finner_fnr() {
        String aktørId = "1856024171652";
        when(aktørRegisterClient.tilFnr(aktørId)).thenThrow(FinnKandidatException.class);
        kandidatService.hentFnr(aktørId);
    }

    @Test
    public void kandidatEksisterer__skal_returnere_true_hvis_kandidat_eksisterer() {
        String aktørId = enAktørId();
        when(repository.hentNyesteKandidat(aktørId)).thenReturn(Optional.of(enKandidat()));
        boolean kandidatEksisterer = kandidatService.kandidatEksisterer(aktørId);
        assertThat(kandidatEksisterer).isTrue();
    }

    @Test
    public void kandidatEksisterer__skal_returnere_false_hvis_kandidat_ikke_eksisterer() {
        String aktørId = enAktørId();
        when(repository.hentNyesteKandidat(aktørId)).thenReturn(Optional.empty());
        boolean kandidatEksisterer = kandidatService.kandidatEksisterer(aktørId);
        assertThat(kandidatEksisterer).isFalse();
    }

}
