package no.nav.finnkandidatapi.midlertidigutilgjengelig;

import no.nav.finnkandidatapi.TestData;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MidlertidigUtilgjengeligControllerTest {

    private MidlertidigUtilgjengeligController controller;

    @Mock
    private MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    @Mock
    private TilgangskontrollService tilgangskontrollService;

    Veileder enVeileder = new Veileder("A100000", "Ola Nordmann");

    @Before
    public void before() {
        controller = new MidlertidigUtilgjengeligController(midlertidigUtilgjengeligService, tilgangskontrollService);

        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(enVeileder);
    }

    @Test
    public void getMidlertidigUtilgjengelig__skal_kunne_hente_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("111111");

        when(midlertidigUtilgjengeligService.hentMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId()))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        var response = controller.getMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody() instanceof MidlertidigUtilgjengelig);
        assertThat((MidlertidigUtilgjengelig)response.getBody()).isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void postMidlertidigUtilgjengelig__skal_kunne_poste_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("2222222");

        MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto = new MidlertidigUtilgjengeligDto(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato());
        when(midlertidigUtilgjengeligService.opprettMidlertidigUtilgjengelig(
                midlertidigUtilgjengeligDto, enVeileder))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        var response = controller.postMidlertidigUtilgjengelig(midlertidigUtilgjengeligDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody() instanceof MidlertidigUtilgjengelig);
        assertThat(response.getBody()).isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void deleteMidlertidigUtilgjengelig__skal_kunne_slette_midlertidig_utilgjengelig() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("2222232");

        when(midlertidigUtilgjengeligService.slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), enVeileder)).thenReturn(1);
        var response = controller.deleteMidlertidigUtilgjenglig(midlertidigUtilgjengelig.getAktørId());

        verify(midlertidigUtilgjengeligService, times(1)).slettMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), enVeileder);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putMidlertidigUtilgjengelig__skal_kunne_oppdatere_tildato() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("333333");
        MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto = new MidlertidigUtilgjengeligDto(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato());

        when(midlertidigUtilgjengeligService.endreMidlertidigTilgjengelig(
                midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato(), enVeileder))
                .thenReturn(Optional.of(midlertidigUtilgjengelig));

        var response = controller.putMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengeligDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(midlertidigUtilgjengelig);
    }

    @Test
    public void putMidlertidigUtilgjengelig__tildato_kan_ikke_være_tilbake_i_tid() {
        MidlertidigUtilgjengelig midlertidigUtilgjengelig = TestData.enMidlertidigUtilgjengelig("7777722");
        midlertidigUtilgjengelig.setTilDato(LocalDateTime.of(2000, 1, 1, 1, 0, 0));

        MidlertidigUtilgjengeligDto midlertidigUtilgjengeligDto = new MidlertidigUtilgjengeligDto(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengelig.getTilDato());

        var response = controller.putMidlertidigUtilgjengelig(midlertidigUtilgjengelig.getAktørId(), midlertidigUtilgjengeligDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
