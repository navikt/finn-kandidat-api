package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligEndret;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligOpprettet;
import no.nav.finnkandidatapi.midlertidigutilgjengelig.event.MidlertidigUtilgjengeligSlettet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MidlertidigUtilgjengeligServiceTest {

    private MidlertidigUtilgjengeligService service;

    @Mock
    private MidlertidigUtilgjengeligRepository repository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;


    Veileder enVeileder = new Veileder("A100000", "Ola Nordmann");

    @Before
    public void before() {
        service = new MidlertidigUtilgjengeligService(repository, applicationEventPublisher);
    }

    @Test
    public void hentMidlertidigUtilgjengelig__kan_hente_midlertidig_utilgjengelig() {

        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("555555");

        when(repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        Optional<MidlertidigUtilgjengelig> response = service.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());
        assertThat(response).isNotEmpty().get().isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void postMidlertidigUtilgjengelig__kan_poste_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("77777777");

        MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto = new MidlertidigUtilgjengeligDto(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato());

        when(repository.lagreMidlertidigUtilgjengelig(any(MidlertidigUtilgjengelig.class)))
                .thenReturn(1);

        when(repository.hentMidlertidigUtilgjengeligMedId(1))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        Optional<MidlertidigUtilgjengelig> response = service.opprettMidlertidigUtilgjengelig(midlertidigUtilgjengeligDto, enVeileder);

        assertThat(response).isEqualTo(Optional.of(midlertidigUtilgjengelig));
        verify(applicationEventPublisher, times(1)).publishEvent(any(MidlertidigUtilgjengeligOpprettet.class));

    }

    @Test
    public void deleteMidlertidigUtilgjengelig__kan_slette_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("32323232");

        when(repository.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(1);

        service.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), enVeileder);

        verify(repository, times(1)).slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());
        verify(applicationEventPublisher, times(1)).publishEvent(any(MidlertidigUtilgjengeligSlettet.class));

    }

    @Test
    public void endreMidlertidigUtilgjengelig__kan_oppdatere_tildato() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("7777722");

        when(repository.endreMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder))
                .thenReturn(1);

        when(repository.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        var response = service.endreMidlertidigTilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder);

        verify(repository, times(1)).endreMidlertidigUtilgjengelig(any(), any(), any());

        assertThat(response).isNotEmpty().get().isEqualTo(midlertidigUtilgjengelig);

        verify(applicationEventPublisher, times(1)).publishEvent(any(MidlertidigUtilgjengeligEndret.class));

    }
}
