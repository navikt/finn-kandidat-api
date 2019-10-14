package no.nav.tag.finnkandidatapi.kafka.republisher;

import no.nav.tag.finnkandidatapi.kafka.HarTilretteleggingsbehov;
import no.nav.tag.finnkandidatapi.kafka.HarTilretteleggingsbehovProducer;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.tag.finnkandidatapi.TestData.enAktørId;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KafkaRepublisherTest {
    private KafkaRepublisher kafkaRepublisher;

    @Mock
    private HarTilretteleggingsbehovProducer producer;

    @Mock
    private KandidatRepository repository;

    @Mock
    private TilgangskontrollService tilgangskontrollService;

    @Mock
    private KafkaRepublisherConfig config;

    @Before
    public void setUp() {
        this.kafkaRepublisher = new KafkaRepublisher(producer, repository, tilgangskontrollService, config);
    }

    @Test(expected = TilgangskontrollException.class)
    public void republiserAlleKandidater__skal_returnere_401_ved_autentiseringsfeil() {
        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(enVeileder());
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(new ArrayList<>());

        kafkaRepublisher.republiserAlleKandidater();
    }

    @Test
    public void republiserAlleKandidater__skal_returnere_200_hvis_suksess() {
        Veileder veileder = enVeileder();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));

        ResponseEntity response = kafkaRepublisher.republiserAlleKandidater();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void republiserAlleKandidater__skal_republisere_hvorvidt_de_siste_kandidatene_har_tilretteleggingsbehov() {
        Veileder veileder = enVeileder();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        when(repository.hentHarTilretteleggingsbehov()).thenReturn(Arrays.asList(
            new HarTilretteleggingsbehov("1000000000001", true),
            new HarTilretteleggingsbehov("1000000000002", false)
        ));

        kafkaRepublisher.republiserAlleKandidater();

        verify(producer, times(2)).sendKafkamelding(any(), any());
        verify(producer).sendKafkamelding("1000000000001", true);
        verify(producer).sendKafkamelding("1000000000002", false);
    }

    @Test(expected = TilgangskontrollException.class)
    public void republiserKandidat__skal_returnere_401_ved_autentiseringsfeil() {
        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(enVeileder());
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(new ArrayList<>());

        kafkaRepublisher.republiserKandidat(enAktørId());
    }

    @Test
    public void republiserKandidat__skal_returnere_404_hvis_kandidat_med_aktørId_ikke_eksisterer() {
        Veileder veileder = enVeileder();
        String aktørId = enAktørId();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        when(repository.hentHarTilretteleggingsbehov(aktørId)).thenReturn(
                Optional.empty()
        );

        ResponseEntity response = kafkaRepublisher.republiserKandidat(aktørId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void republiserKandidat__skal_returnere_200_hvis_suksess() {
        Veileder veileder = enVeileder();
        String aktørId = enAktørId();

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(veileder);
        when(config.getNavIdenterSomKanRepublisere()).thenReturn(Arrays.asList(veileder.getNavIdent()));
        when(repository.hentHarTilretteleggingsbehov(enAktørId())).thenReturn(
                Optional.of(new HarTilretteleggingsbehov(aktørId, true))
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
        when(repository.hentHarTilretteleggingsbehov(aktørId)).thenReturn(
                Optional.of(new HarTilretteleggingsbehov(aktørId, true))
        );

        kafkaRepublisher.republiserKandidat(aktørId);

        verify(producer).sendKafkamelding(aktørId, true);
    }
}
