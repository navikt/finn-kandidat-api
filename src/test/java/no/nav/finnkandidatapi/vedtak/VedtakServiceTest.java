package no.nav.finnkandidatapi.vedtak;


import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.finnkandidatapi.kafka.vedtakReplikert.VedtakReplikert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static no.nav.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedtakServiceTest {

    private VedtakService vedtakService;

    @Mock
    private VedtakRepository vedtakRepository;

    @Mock
    private AktorOppslagClient aktorOppslagClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Before
    public void setUp() {
        when(aktorOppslagClient.hentAktorId(any())).thenReturn(new AktorId("1000000000001"));
        vedtakService = new VedtakService(vedtakRepository, aktorOppslagClient, eventPublisher);
    }

    @Test
    public void permittering_vedtak_skal_lagres_og_event_publiseres() {
        Long id = 1234L;
        when(vedtakRepository.lagreVedtak(any())).thenReturn(id);

        VedtakReplikert vedtakReplikert = etUpdateVedtakReplikert();
        vedtakService.behandleVedtakReplikert(vedtakReplikert);

        verify(vedtakRepository, times(1)).lagreVedtak(any());
        ArgumentCaptor<VedtakEndret> argument = ArgumentCaptor.forClass(VedtakEndret.class);
        verify(eventPublisher).publishEvent(argument.capture());
        assertThat(argument.getValue().getVedtak().getId()).isEqualTo(id);
    }

    @Test
    public void delete_operasjoner_skal_lagres_og_markeres_som_slettet() {
        Long id = 1234L;
        when(vedtakRepository.lagreVedtak(any())).thenReturn(id);
        when(vedtakRepository.logiskSlettVedtak(any())).thenReturn(1);

        VedtakReplikert vedtakReplikert = etDeleteVedtakReplikert();
        vedtakService.behandleVedtakReplikert(vedtakReplikert);

        verify(vedtakRepository, times(1)).lagreVedtak(any());
        verify(vedtakRepository, times(1)).logiskSlettVedtak(any());
        ArgumentCaptor<VedtakSlettet> argument = ArgumentCaptor.forClass(VedtakSlettet.class);
        verify(eventPublisher).publishEvent(argument.capture());
        assertThat(argument.getValue().getVedtak().getId()).isEqualTo(id);
    }

    @Test
    public void feil_ved_sletting_skal_kaste_exception() {
        Long id = 1234L;
        when(vedtakRepository.lagreVedtak(any())).thenReturn(id);
        when(vedtakRepository.logiskSlettVedtak(any())).thenReturn(0);

        VedtakReplikert vedtakReplikert = etDeleteVedtakReplikert();
        Throwable thrown = catchThrowable(() -> {
            vedtakService.behandleVedtakReplikert(vedtakReplikert);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class).hasNoCause();
        verify(vedtakRepository, times(1)).lagreVedtak(any());
        verify(vedtakRepository, times(1)).logiskSlettVedtak(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    public void skal_ignorere_update_vedtak_med_utfallskode_nei() {
        VedtakReplikert vedtak = etAvslåttUpdateVedtakReplikert();
        vedtakService.behandleVedtakReplikert(vedtak);
        verify(vedtakRepository, times(0)).lagreVedtak(any());
    }

    @Test
    public void skal_ignorere_delete_vedtak_med_utfallskode_nei() {
        VedtakReplikert vedtak = etAvslåttDeleteVedtakReplikert();
        vedtakService.behandleVedtakReplikert(vedtak);
        verify(vedtakRepository, times(0)).lagreVedtak(any());
    }

    @Test
    public void skal_ignorere_insert_vedtak_med_utfallskode_nei() {
        VedtakReplikert vedtak = etAvslåttInsertVedtakReplikert();
        vedtakService.behandleVedtakReplikert(vedtak);
        verify(vedtakRepository, times(0)).lagreVedtak(any());
    }
}
