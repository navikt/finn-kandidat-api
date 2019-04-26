package no.nav.tag.finnkandidatapi.kandidat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KandidatControllerTest {

    @InjectMocks
    private KandidatController controller;

    @Mock
    private KandidatRepository repository;

    @Mock
    private KandidatService service;

    @Mock
    private TokenUtils tokenUtils;

    @Test
    public void lagreKandidat__skal_returnere_created_med_opprettet_kandidat() {
        værInnloggetSom(enVeileder());
        Kandidat kandidat = enKandidat();

        when(repository.lagreKandidat(any(Kandidat.class))).thenReturn(1);
        when(repository.hentKandidat(1)).thenReturn(kandidat);

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

        controller.lagreKandidat(kandidat);

        verify(service).oppdaterKandidat(kandidat, veileder);
    }

    private void værInnloggetSom(Veileder veileder) {
        when(tokenUtils.hentInnloggetVeileder()).thenReturn(veileder);
    }

}
