package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.Brukertype;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.metrikker.KandidatEndret;
import no.nav.finnkandidatapi.metrikker.KandidatOpprettet;
import no.nav.finnkandidatapi.metrikker.KandidatSlettet;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KandidatHandlerTest {

    private KandidatHandler kandidatHandler;

    private String aktørid = "12335";

    @Mock
    public KandidatRepository kandidatRepository;

    @Mock
    public VedtakService vedtakService;

    @Mock
    public PermittertArbeidssokerService permittertArbeidssokerService;

    @Mock
    HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;

    @Mock
    AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer;


    @Before
    public void before() {
        kandidatHandler = new KandidatHandler(
                new SammenstillBehov(
                        kandidatRepository,
                        permittertArbeidssokerService,
                        vedtakService
                ), harTilretteleggingsbehovProducer, aivenHarTilretteleggingsbehovProducer

        );

        PermittertArbeidssoker permittertArbeidssoker = TestData.enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørid);
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);

        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.of(permittertArbeidssoker));
        when(vedtakService.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
    }


    @Test
    public void legg_inn_event_opprettet_og_endret() {
        Kandidat kandidat = TestData.enKandidat();
        kandidat.setAktørId(aktørid);
        kandidat.setFysisk(null);

        kandidatHandler.kandidatOpprettet(new KandidatOpprettet(kandidat));
        kandidatHandler.kandidatEndret(new KandidatEndret(kandidat));

        verify(harTilretteleggingsbehovProducer, times(2)).sendKafkamelding(
                new HarTilretteleggingsbehov(
                        aktørid,
                        true,
                        Arrays.asList(
                                "arbeidstid",
                                "arbeidshverdagen",
                                "utfordringerMedNorsk",
                                PermittertArbeidssoker.ER_PERMITTERT_KATEGORI
                        ))
        );
    }

    @Test
    public void legg_inn_event_slettet() {
        Kandidat kandidat = TestData.enKandidat();
        kandidat.setAktørId(aktørid);
        kandidat.setFysisk(null);

        kandidatHandler.kandidatSlettet(new KandidatSlettet(
                1, aktørid, Brukertype.VEILEDER, LocalDateTime.now().minusDays(1)));

        verify(harTilretteleggingsbehovProducer, times(1)).sendKafkamelding(
                new HarTilretteleggingsbehov(
                        aktørid,
                        false,
                        Arrays.asList(PermittertArbeidssoker.ER_PERMITTERT_KATEGORI))
        );
    }
}