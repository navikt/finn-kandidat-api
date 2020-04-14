package no.nav.finnkandidatapi.vedtak;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedtakServiceTest {

    private VedtakService vedtakService;

    @Mock
    private VedtakRepository vedtakRepository;

    @Mock
    private AktørRegisterClient aktørRegisterClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Before
    public void setUp() {
        when(meterRegistry.counter(anyString())).thenReturn(counter);
        vedtakService = new VedtakService(vedtakRepository, aktørRegisterClient, eventPublisher, meterRegistry);
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
        verifyNoInteractions(meterRegistry);
    }
}