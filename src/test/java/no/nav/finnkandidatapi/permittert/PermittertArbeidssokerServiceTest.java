package no.nav.finnkandidatapi.permittert;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.finnkandidatapi.DateProvider;
import no.nav.finnkandidatapi.kandidat.KandidatService;
import no.nav.finnkandidatapi.metrikker.PermittertArbeidssokerEndretEllerOpprettet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.*;
import static no.nav.finnkandidatapi.unleash.UnleashConfiguration.HENT_OPPFØLGINGSBRUKER_VED_OPPRETT_KANDIDAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PermittertArbeidssokerServiceTest {

    private PermittertArbeidssokerService service;

    @Mock
    private PermittertArbeidssokerRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Before
    public void setUp() {
        service = new PermittertArbeidssokerService(
                repository,
                eventPublisher);
    }

    @Test
    public void behandling_av_registrering_av_kjent_arbeidssoker_skal_føre_til_at_event_publiseres() {

        ArbeidssokerRegistrertDTO enRegistrering = enKjentArbeidssokerRegistrering();
        PermittertArbeidssoker enArbeidssoker = enPermittertArbeidssoker();
        when(repository.hentNyestePermittertArbeidssoker(enRegistrering.getAktørId())).thenReturn(Optional.of(enArbeidssoker));
        when(repository.lagrePermittertArbeidssoker(any())).thenReturn(100);
        when(repository.hentPermittertArbeidssoker(100)).thenReturn(Optional.of(enArbeidssoker));

        service.behandleArbeidssokerRegistrert(enRegistrering);

        verify(eventPublisher, times(1)).publishEvent(any(PermittertArbeidssokerEndretEllerOpprettet.class));
    }

    @Test
    public void behandling_av_registrering_av_ukjent_arbeidssoker_skal_føre_til_at_event_publiseres() {

        ArbeidssokerRegistrertDTO enRegistrering = enUkjentArbeidssokerRegistrering();
        PermittertArbeidssoker enArbeidssoker = enPermittertArbeidssoker();
        when(repository.hentNyestePermittertArbeidssoker(enRegistrering.getAktørId())).thenReturn(Optional.empty());
        when(repository.lagrePermittertArbeidssoker(any())).thenReturn(100);
        when(repository.hentPermittertArbeidssoker(100)).thenReturn(Optional.of(enArbeidssoker));

        service.behandleArbeidssokerRegistrert(enRegistrering);

        verify(eventPublisher, times(1)).publishEvent(any(PermittertArbeidssokerEndretEllerOpprettet.class));
    }

    @Test
    public void behandling_av_registrering_av_ukjent_arbeidssoker_skal_føre_til_opprettelse_av_arbeidssoker_i_db() {

        ArbeidssokerRegistrertDTO enRegistrering = enUkjentArbeidssokerRegistrering();
        PermittertArbeidssoker enArbeidssoker = enPermittertArbeidssoker();
        PermittertArbeidssoker enLagretArbeidssoker = enLagretPermittertArbeidssoker(100);

        when(repository.hentNyestePermittertArbeidssoker(eq(enRegistrering.getAktørId()))).thenReturn(Optional.empty());

        //Det under her tester jeg ikke på nå, dette er bare for ikke å få exceptions
        when(repository.lagrePermittertArbeidssoker(any())).thenReturn(101);
        when(repository.hentPermittertArbeidssoker(any())).thenReturn(Optional.of(enLagretArbeidssoker));

        service.behandleArbeidssokerRegistrert(enRegistrering);

        ArgumentCaptor<PermittertArbeidssoker> argument = ArgumentCaptor.forClass(PermittertArbeidssoker.class);
        verify(repository).lagrePermittertArbeidssoker(argument.capture());

        assertThat(argument.getValue().getId()).isNull();
    }

    @Test
    public void behandling_av_registrering_av_kjent_arbeidssoker_skal_føre_til_endring_av_arbeidssoker_i_db() {

        ArbeidssokerRegistrertDTO enRegistrering = enUkjentArbeidssokerRegistrering();
        PermittertArbeidssoker enArbeidssoker = enPermittertArbeidssoker();
        PermittertArbeidssoker enLagretArbeidssoker = enLagretPermittertArbeidssoker(100);

        when(repository.hentNyestePermittertArbeidssoker(eq(enRegistrering.getAktørId()))).thenReturn(Optional.of(enLagretArbeidssoker));

        //Det under her tester jeg ikke på nå, dette er bare for ikke å få exceptions
        when(repository.lagrePermittertArbeidssoker(any())).thenReturn(101);
        when(repository.hentPermittertArbeidssoker(any())).thenReturn(Optional.of(enLagretArbeidssoker));

        service.behandleArbeidssokerRegistrert(enRegistrering);

        ArgumentCaptor<PermittertArbeidssoker> argument = ArgumentCaptor.forClass(PermittertArbeidssoker.class);
        verify(repository).lagrePermittertArbeidssoker(argument.capture());

        assertThat(argument.getValue().getId()).isNotNull();
        assertThat(argument.getValue().getId()).isEqualTo(100);
    }
}
