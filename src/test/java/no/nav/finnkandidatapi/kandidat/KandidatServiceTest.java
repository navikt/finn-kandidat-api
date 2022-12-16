package no.nav.finnkandidatapi.kandidat;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.kafka.oppfølgingAvsluttet.OppfølgingAvsluttetMelding;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static no.nav.finnkandidatapi.TestData.*;
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
    private AktorOppslagClient aktorOppslagClient;

    @Before
    public void setUp() {
        kandidatService = new KandidatService(
                repository,
                eventPublisher,
                aktorOppslagClient,
                dateProvider
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
    public void opprettKandidat__skal_opprette_kandidat_med_riktige_felter() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        kandidat.setSistEndretAv(veileder.getNavIdent());

        Kandidat kandidatTilOpprettelse = Kandidat.opprettKandidat(
                kandidatDto,
                veileder,
                kandidat.getSistEndretAvVeileder()
        );

        when(dateProvider.now()).thenReturn(kandidat.getSistEndretAvVeileder());
        when(repository.lagreKandidatSomVeileder(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidatTilOpprettelse));

        Kandidat opprettetKandidat = kandidatService.opprettKandidat(kandidatDto, veileder).get();

        verify(repository).lagreKandidatSomVeileder(opprettetKandidat);
    }

    @Test
    public void opprettKandidat__skal_returnere_empty_hvis_kandidat_ikke_ble_lagret() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Veileder veileder = enVeileder();
        kandidat.setSistEndretAv(veileder.getNavIdent());

        when(dateProvider.now()).thenReturn(kandidat.getSistEndretAvVeileder());
        when(repository.lagreKandidatSomVeileder(kandidat)).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.empty());

        Optional<Kandidat> empty = kandidatService.opprettKandidat(kandidatDto, veileder);

        Assertions.assertThat(empty).isEmpty();
    }

    @Test
    public void opprettKandidat__skal_publisere_KandidatOpprettet_event() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto(kandidat);
        Integer kandidatId = 1;

        when(dateProvider.now()).thenReturn(kandidat.getSistEndretAvVeileder());
        when(repository.lagreKandidatSomVeileder(any(Kandidat.class))).thenReturn(kandidatId);
        when(repository.hentKandidat(kandidatId)).thenReturn(Optional.of(kandidat));

        kandidatService.opprettKandidat(kandidatDto, enVeileder());
        verify(eventPublisher).publishEvent(new KandidatOpprettet(kandidat));
    }

    @Test
    public void endreKandidat__skal_endre_kandidatens_felter() {
        Kandidat kandidat = enKandidat();
        KandidatDto kandidatDto = enKandidatDto();
        Veileder veileder = enVeileder();
        LocalDateTime datetime = now();
        when(dateProvider.now()).thenReturn(datetime);
        Kandidat kandidatTilLagring = Kandidat.endreKandidat(kandidat, kandidatDto, veileder, dateProvider.now());

        when(repository.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));
        when(repository.lagreKandidatSomVeileder(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidatTilLagring));

        Kandidat endretKandidat = kandidatService.endreKandidat(kandidatDto, veileder).get();

        verify(repository).lagreKandidatSomVeileder(endretKandidat);
    }

    @Test
    public void endreKandidat__skal_returnere_empty_hvis_ingen_kandidat() {
        KandidatDto kandidatDto = enKandidatDto();
        when(repository.hentNyesteKandidat(kandidatDto.getAktørId())).thenReturn(Optional.empty());
        Optional<Kandidat> endretKandidat = kandidatService.endreKandidat(kandidatDto, enVeileder());
        Assertions.assertThat(endretKandidat).isEmpty();
    }

    @Test
    public void endreKandidat__skal_returnere_empty_hvis_man_ikke_kunne_lagre_kandidat() {
        KandidatDto kandidatDto = enKandidatDto();
        when(repository.hentNyesteKandidat(kandidatDto.getAktørId())).thenReturn(Optional.empty());
        Optional<Kandidat> endretKandidat = kandidatService.endreKandidat(enKandidatDto(), enVeileder());
        Assertions.assertThat(endretKandidat).isEmpty();
    }

    @Test
    public void endreKandidat__skal_publisere_Kandidatoppdatering_event() {
        Kandidat kandidat = enKandidat();
        when(repository.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));
        when(repository.lagreKandidatSomVeileder(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(Optional.of(kandidat));

        Kandidat endretKandidat = kandidatService.endreKandidat(enKandidatDto(), enVeileder()).get();

        verify(eventPublisher).publishEvent(new KandidatEndret(endretKandidat));
    }

    @Test
    public void slettKandidat_skal_slette_kandidat_med_riktig_aktør_id_veilder_og_tidspunkt() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = now();
        when(dateProvider.now()).thenReturn(datetime);

        kandidatService.slettKandidat(aktørId, veileder);

        verify(repository).slettKandidatSomVeileder(aktørId, veileder, datetime);
    }

    @Test
    public void slettKandidat_skal_publisere_KandidatSlettet_event() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();
        LocalDateTime datetime = now();

        when(dateProvider.now()).thenReturn(datetime);
        Optional<Integer> slettetKey = Optional.of(4);
        when(repository.slettKandidatSomVeileder(aktørId, veileder, datetime)).thenReturn(slettetKey);

        kandidatService.slettKandidat(aktørId, veileder);

        verify(eventPublisher).publishEvent(new KandidatSlettet(slettetKey.get(), aktørId, Brukertype.VEILEDER, datetime));
    }

    @Test
    public void slettKandidat_skal_returnere_id() {
        String aktørId = "1000000000001";
        Veileder veileder = enVeileder();

        when(repository.slettKandidatSomVeileder(eq(aktørId), eq(veileder), any())).thenReturn(Optional.of(4));

        assertThat(kandidatService.slettKandidat(aktørId, veileder).get()).isEqualTo(4);
    }

    @Test
    public void behandleOppfølgingAvsluttet__skal_slette_kandidat() {
        String aktørId = "1000000000001";

        kandidatService.behandleOppfølgingAvsluttet(new OppfølgingAvsluttetMelding(aktørId, new Date()));

        verify(repository).slettKandidatSomMaskinbruker(aktørId, dateProvider.now());
    }

    @Test
    public void behandleOppfølgingAvsluttet_nyttTopic__skal_slette_kandidat() {
        String aktørId = "1000000000001";

        kandidatService.behandleOppfølgingAvsluttet(new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                aktørId,
                ZonedDateTime.now().minusYears(2),
                ZonedDateTime.now()
        ));

        verify(repository).slettKandidatSomMaskinbruker(aktørId, dateProvider.now());
    }

    @Test
    public void behandleOppfølgingAvsluttet__skal_publisere_KandidatSlettet_event() {
        String aktørId = "1856024171652";
        LocalDateTime datetime = now();
        when(dateProvider.now()).thenReturn(datetime);
        Optional<Integer> slettetKey = Optional.of(4);
        when(repository.slettKandidatSomMaskinbruker(aktørId, datetime)).thenReturn(slettetKey);

        kandidatService.behandleOppfølgingAvsluttet(new OppfølgingAvsluttetMelding(aktørId, new Date()));

        verify(eventPublisher).publishEvent(new KandidatSlettet(slettetKey.get(), aktørId, Brukertype.SYSTEM, datetime));
    }

    @Test
    public void behandleOppfølgingAvsluttet_nyttTopic__skal_publisere_KandidatSlettet_event() {
        String aktørId = "1856024171652";
        LocalDateTime datetime = now();
        when(dateProvider.now()).thenReturn(datetime);
        Optional<Integer> slettetKey = Optional.of(4);
        when(repository.slettKandidatSomMaskinbruker(aktørId, datetime)).thenReturn(slettetKey);

        kandidatService.behandleOppfølgingAvsluttet(new SisteOppfolgingsperiodeV1(
                UUID.randomUUID(),
                aktørId,
                ZonedDateTime.now().minusYears(2),
                ZonedDateTime.now()
        ));

        verify(eventPublisher).publishEvent(new KandidatSlettet(slettetKey.get(), aktørId, Brukertype.SYSTEM, datetime));
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
