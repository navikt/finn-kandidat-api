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
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MidlertidigUtilgjengeligControllerTest {

    private MidlertidigUtilgjengeligController controller;

    @Mock
    private MidlertidigUtilgjengeligService midlertidigUtilgjengeligService;

    @Mock
    private TilgangskontrollService tilgangskontrollService;

    @Before
    public void before() {
        controller = new MidlertidigUtilgjengeligController(midlertidigUtilgjengeligService, tilgangskontrollService);
        when(tilgangskontrollService.hentInnloggetVeileder()).thenReturn(new Veileder("A100000", "Ola Nordmann"));
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


}
