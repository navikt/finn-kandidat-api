package no.nav.finnkandidatapi.kafka.republisher;

import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.Fysisk;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class
KafkaRepublisherTest {
    private KafkaRepublisher kafkaRepublisher;

    @Mock
    private AivenHarTilretteleggingsbehovProducer producer;

    @Mock
    private KandidatRepository kandidatRepository;

    @Mock
    private RepublisherRepository republisherRepository;

    @Mock
    private TilgangskontrollService tilgangskontrollService;

    @Mock
    private KafkaRepublisherConfig config;

    @Mock
    private PermittertArbeidssokerService permittertArbeidssokerService;

    @Mock
    private VedtakService vedtakService;

    private SammenstillBehov sammenstillBehov;

    @Before
    public void setUp() {
        this.sammenstillBehov = new SammenstillBehov(kandidatRepository, permittertArbeidssokerService, vedtakService);
        this.kafkaRepublisher = new KafkaRepublisher(producer, kandidatRepository,  republisherRepository, tilgangskontrollService, sammenstillBehov, config);
    }

    @Test(expected = TilgangskontrollException.class)
    public void republiserAlleKandidater__skal_returnere_401_ved_autentiseringsfeil() {
        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(enVeileder());
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(new ArrayList<>());

        kafkaRepublisher.republiserAlleTilretteleggingsbehov();
    }

    @Test
    public void republiserAlleKandidater__skal_returnere_200_hvis_suksess() {
        Veileder veileder = enVeileder();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));

        ResponseEntity response = kafkaRepublisher.republiserAlleTilretteleggingsbehov();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void republiserAlleKandidater__skal_republisere_hvorvidt_de_siste_kandidatene_har_tilretteleggingsbehov() {
        Veileder veileder = enVeileder();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        HarTilretteleggingsbehov harTilretteleggingsbehovTrue = new HarTilretteleggingsbehov("1000000000001", true, List.of(Fysisk.behovskategori));
        HarTilretteleggingsbehov harTilretteleggingsbehovFalse = new HarTilretteleggingsbehov("1000000000002", false, List.of());
        when(kandidatRepository.hentHarTilretteleggingsbehov()).thenReturn(Arrays.asList(
                harTilretteleggingsbehovTrue,
                harTilretteleggingsbehovFalse
        ));

        kafkaRepublisher.republiserAlleTilretteleggingsbehov();

        verify(producer, times(2)).sendKafkamelding(any());
        verify(producer).sendKafkamelding(harTilretteleggingsbehovTrue);
        verify(producer).sendKafkamelding(harTilretteleggingsbehovFalse);
    }

    @Test(expected = TilgangskontrollException.class)
    public void republiserKandidat__skal_returnere_401_ved_autentiseringsfeil() {
        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(enVeileder());
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(new ArrayList<>());

        kafkaRepublisher.republiserKandidat(enAktørId());
    }

    @Test
    public void republiserKandidat__skal_returnere_200_hvis_suksess() {
        Veileder veileder = enVeileder();
        String aktørId = enAktørId();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        when(kandidatRepository.hentHarTilretteleggingsbehov(enAktørId())).thenReturn(
                Optional.of(new HarTilretteleggingsbehov(aktørId, true, List.of(Fysisk.behovskategori)))
        );

        ResponseEntity response = kafkaRepublisher.republiserKandidat(aktørId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void republiserKandidat__skal_republisere_hvorvidt_kandidaten_har_tilretteleggingsbehov() {
        Veileder veileder = enVeileder();
        String aktørId = enAktørId();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        HarTilretteleggingsbehov harTilretteleggingsbehov = new HarTilretteleggingsbehov(aktørId, true, List.of(Fysisk.behovskategori));
        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørId)).thenReturn(
                Optional.of(harTilretteleggingsbehov)
        );

        kafkaRepublisher.republiserKandidat(aktørId);

        verify(producer).sendKafkamelding(harTilretteleggingsbehov);
    }

    @Test
    public void republiserKandidat__skal_sende_med_permittertstatus() {
        Veileder veileder = enVeileder();
        String aktørId = enAktørId();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        HarTilretteleggingsbehov harTilretteleggingsbehov = new HarTilretteleggingsbehov(aktørId, true, List.of(Fysisk.behovskategori));
        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørId)).thenReturn(
                Optional.of(harTilretteleggingsbehov)
        );
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørId)).thenReturn(
                Optional.of(enPermittertArbeidssoker())
        );

        kafkaRepublisher.republiserKandidat(aktørId);

        HarTilretteleggingsbehov forventetBehov = new HarTilretteleggingsbehov(
                aktørId,
                true,
                List.of(Fysisk.behovskategori, PermittertArbeidssoker.ER_PERMITTERT_KATEGORI)
        );
        verify(producer).sendKafkamelding(forventetBehov);
    }
}
