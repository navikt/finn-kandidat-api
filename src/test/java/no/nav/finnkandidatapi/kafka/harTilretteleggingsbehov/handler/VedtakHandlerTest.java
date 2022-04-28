package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedtakHandlerTest {
    private VedtakHandler vedtakHandler;

    private String aktørid = "12336";

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
        vedtakHandler = new VedtakHandler(
                new SammenstillBehov(
                        kandidatRepository,
                        permittertArbeidssokerService,
                        vedtakService
                ), harTilretteleggingsbehovProducer, aivenHarTilretteleggingsbehovProducer

        );

        HarTilretteleggingsbehov harTilretteleggingsbehov =
                new HarTilretteleggingsbehov(aktørid, true, TestData.enKandidat().kategorier());
        PermittertArbeidssoker permittertArbeidssoker = TestData.enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørid);

        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørid)).thenReturn(Optional.of(harTilretteleggingsbehov));
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.of(permittertArbeidssoker));
    }

    @Test
    public void legg_inn_event() {
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);

        vedtakHandler.vedtakOpprettet(new VedtakOpprettet(vedtak));
        vedtakHandler.vedtakEndret(new VedtakEndret(vedtak));
        vedtakHandler.vedtakSlettet(new VedtakSlettet(vedtak));

        verify(harTilretteleggingsbehovProducer, times(3)).sendKafkamelding(
                new HarTilretteleggingsbehov(
                        aktørid,
                        true,
                        Arrays.asList(
                                "arbeidstid",
                                "fysisk",
                                "arbeidshverdagen",
                                "utfordringerMedNorsk",
                                PermittertArbeidssoker.ER_PERMITTERT_KATEGORI
                                ))
        );
    }
}