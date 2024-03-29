package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.AivenHarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssoker;
import no.nav.finnkandidatapi.permittert.PermittertArbeidssokerService;
import no.nav.finnkandidatapi.vedtak.Vedtak;
import no.nav.finnkandidatapi.vedtak.VedtakService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PermitteringHandlerTest {
    private PermitteringHandler permitteringHandler;

    private String aktørid = "12336";

    @Mock
    public KandidatRepository kandidatRepository;

    @Mock
    public VedtakService vedtakService;

    @Mock
    public PermittertArbeidssokerService permittertArbeidssokerService;

    @Mock
    AivenHarTilretteleggingsbehovProducer aivenHarTilretteleggingsbehovProducer;


    @Before
    public void before() {
        permitteringHandler = new PermitteringHandler(
                new SammenstillBehov(
                        kandidatRepository,
                        permittertArbeidssokerService,
                        vedtakService
                ), aivenHarTilretteleggingsbehovProducer

        );

        HarTilretteleggingsbehov harTilretteleggingsbehov =
                new HarTilretteleggingsbehov(aktørid, true, TestData.enKandidat().kategorier());
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);

        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørid)).thenReturn(Optional.of(harTilretteleggingsbehov));
        when(vedtakService.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
    }

    @Test
    public void legg_inn_event() {
        PermittertArbeidssoker permittertArbeidssoker = TestData.enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørid);
        permitteringHandler.permitteringEndretEllerOpprettet(
                new PermittertArbeidssokerEndretEllerOpprettet(permittertArbeidssoker)
        );
        verify(aivenHarTilretteleggingsbehovProducer, times(1)).sendKafkamelding(
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