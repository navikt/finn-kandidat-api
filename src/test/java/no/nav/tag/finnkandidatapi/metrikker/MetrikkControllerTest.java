package no.nav.tag.finnkandidatapi.metrikker;

import no.nav.tag.finnkandidatapi.TestData;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.kandidat.KandidatRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrikkControllerTest {
    private MetrikkController metrikkController;

    @Mock
    KandidatRepository kandidatRepository;

    @Before
    public void setUp() {
        this.metrikkController = new MetrikkController(kandidatRepository);
    }

    @Test
    public void hentAndelKandidaterMedNavkontor__skal_hente_andel_kandidater_med_nav_kontor() {
        Kandidat kandidatMedNavkontor = TestData.enKandidat();
        Kandidat kandidatUtenNavkontor = TestData.enKandidat();
        kandidatUtenNavkontor.setNavKontor(null);

        when(kandidatRepository.hentKandidater()).thenReturn(List.of(
            kandidatMedNavkontor,
            kandidatUtenNavkontor
        ));

        ResponseEntity<String> respons = metrikkController.hentAndelKandidaterMedNavkontor();

        assertThat(respons.getBody()).contains("1 av 2");
    }
}
