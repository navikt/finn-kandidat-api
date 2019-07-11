package no.nav.tag.finnkandidatapi.tilbakemelding;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import static no.nav.tag.finnkandidatapi.TestData.enTilbakemelding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class TilbakemeldingControllerTest {

    @Mock
    private TilbakemeldingRepository repository;

    private TilbakemeldingController tilbakemeldingController;

    @Before
    public void setUp() {
        tilbakemeldingController = new TilbakemeldingController(repository);
    }

    @Test
    public void giTilbakemelding__skal_lagre_tilbakemelding() {
        Tilbakemelding tilbakemelding = enTilbakemelding();
        tilbakemeldingController.giTilbakemelding(tilbakemelding);
        verify(repository, times(1)).lagreTilbakemelding(tilbakemelding);
    }

    @Test
    public void giTilbakemelding__skal_returnere_201_created() {
        Tilbakemelding tilbakemelding = enTilbakemelding();
        assertThat(tilbakemeldingController.giTilbakemelding(tilbakemelding).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}