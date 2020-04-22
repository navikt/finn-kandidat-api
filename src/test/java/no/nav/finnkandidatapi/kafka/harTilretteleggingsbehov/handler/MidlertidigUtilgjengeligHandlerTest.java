package no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.handler;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehovProducer;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.SammenstillBehov;
import no.nav.finnkandidatapi.kandidat.KandidatRepository;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengelig;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.MidlertidigUtilgjengeligService;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
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
public class MidlertidigUtilgjengeligHandlerTest {

    private MidlertidigUtilgjengeligHandler midlertidigUtilgjengeligHandler;

    private String aktørid = "12334";

    @Mock
    public KandidatRepository kandidatRepository;

    @Mock
    public VedtakService vedtakService;

    @Mock
    public PermittertArbeidssokerService permittertArbeidssokerService;

    @Mock
    MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    @Mock
    HarTilretteleggingsbehovProducer harTilretteleggingsbehovProducer;


    @Before
    public void before() {
        midlertidigUtilgjengeligHandler = new MidlertidigUtilgjengeligHandler(
                new SammenstillBehov(
                        kandidatRepository,
                        permittertArbeidssokerService,
                        vedtakService,
                        midlertidigUtilgjengeligService
                ), harTilretteleggingsbehovProducer

        );

        HarTilretteleggingsbehov harTilretteleggingsbehov =
                new HarTilretteleggingsbehov(aktørid, true, TestData.enKandidat().kategorier());
        PermittertArbeidssoker permittertArbeidssoker = TestData.enPermittertArbeidssoker();
        permittertArbeidssoker.setAktørId(aktørid);
        Vedtak vedtak = TestData.etVedtak();
        vedtak.setAktørId(aktørid);

        when(kandidatRepository.hentHarTilretteleggingsbehov(aktørid)).thenReturn(Optional.of(harTilretteleggingsbehov));
        when(permittertArbeidssokerService.hentNyestePermitterteArbeidssoker(aktørid)).thenReturn(Optional.of(permittertArbeidssoker));
        when(vedtakService.hentNyesteVedtakForAktør(aktørid)).thenReturn(Optional.of(vedtak));
    }

    @Test
    public void legg_inn_og_oppdater_event() {

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig(aktørid);
        midlertidigUtilgjengelig.setTilDato(LocalDateTime.now().plusDays(1));
        midlertidigUtilgjengeligHandler.midlertidigUtilgjengeligOpprettet(
                new MidlertidigUtilgjengeligOpprettet(midlertidigUtilgjengelig)
        );
        midlertidigUtilgjengeligHandler.midlertidigUtilgjengeligEndret(
                new MidlertidigUtilgjengeligEndret(midlertidigUtilgjengelig)
        );
        verify(harTilretteleggingsbehovProducer, times(2)).sendKafkamelding(
                new HarTilretteleggingsbehov(
                        aktørid,
                        true,
                        Arrays.asList(
                                "arbeidstid",
                                "fysisk",
                                "arbeidshverdagen",
                                "utfordringerMedNorsk",
                                PermittertArbeidssoker.ER_PERMITTERT_KATEGORI,
                                MidlertidigUtilgjengelig.MIDLERTIDIG_UTILGJENGELIG_1_UKE
                        ))
        );
    }

    @Test
    public void legg_inn_event_slettet() {

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig(aktørid);
        midlertidigUtilgjengelig.setTilDato(LocalDateTime.now().plusDays(1));

        midlertidigUtilgjengeligHandler.midlertidigUtilgjengeligSlettet(
                new MidlertidigUtilgjengeligSlettet(midlertidigUtilgjengelig)
        );
        verify(harTilretteleggingsbehovProducer, times(1)).sendKafkamelding(
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