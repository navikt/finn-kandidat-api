package no.nav.finnkandidatapi.veileder;

import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static no.nav.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VeilederControllerTest {
    private VeilederController controller;

    @Mock
    private TilgangskontrollService tilgangskontroll;

    @Before
    public void setUp() {
        controller = new VeilederController(tilgangskontroll);
    }

    @Test
    public void hentInnloggetVeileder__skal_returnere_innlogget_veileder_sin_navident() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        ResponseEntity respons = controller.hentInnloggetVeileder();
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(veileder.getNavIdent());
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tilgangskontroll.hentInnloggetVeileder()).thenReturn(veileder);
    }
}
