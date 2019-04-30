package no.nav.tag.finnkandidatapi.kandidat;

import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KandidatControllerTest {

    @InjectMocks
    private KandidatController controller;

    @Mock
    private KandidatService service;

    @Mock
    private TokenUtils tokenUtils;

    @Test
    public void lagreKandidat__skal_returnere_created_med_opprettet_kandidat() {
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);
        Kandidat kandidat = enKandidat();

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.lagreKandidat(kandidat);
        Kandidat hentetKandidat = respons.getBody();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(hentetKandidat).isEqualTo(kandidat);
    }

    @Test
    public void lagreKandidat__skal_kalle_kandidat_service_med_riktige_parameter() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.of(kandidat));

        controller.lagreKandidat(kandidat);

        verify(service).lagreKandidat(kandidat, veileder);
    }

    @Test(expected = NotFoundException.class)
    public void hentKandidat__skal_kaste_NotFoundException_hvis_kandidat_ikke_fins() {
        String fnr = enKandidat().getFnr();
        when(service.hentNyesteKandidat(fnr)).thenReturn(Optional.empty());
        controller.hentKandidat(fnr);
    }

    @Test
    public void hentKandidat__skal_returnere_ok_med_kandidat() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(service.hentNyesteKandidat(kandidat.getFnr())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentKandidat(kandidat.getFnr());

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test(expected = FinnKandidatException.class)
    public void lagreKandidat__skal_kaste_FinnKandidatException_hvis_kandidat_ikke_fins() {
        Kandidat kandidat = enKandidat();
        Veileder veileder = enVeileder();
        værInnloggetSom(veileder);

        when(service.lagreKandidat(kandidat, veileder)).thenReturn(Optional.empty());
        controller.lagreKandidat(kandidat);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tokenUtils.hentInnloggetVeileder()).thenReturn(veileder);
    }
}
