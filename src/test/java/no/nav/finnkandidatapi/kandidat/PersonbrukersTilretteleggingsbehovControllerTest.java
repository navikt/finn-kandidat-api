package no.nav.finnkandidatapi.kandidat;

import no.nav.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static no.nav.finnkandidatapi.TestData.enKandidat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonbrukersTilretteleggingsbehovControllerTest {

    private PersonbrukersTilretteleggingsbehovController controller;

    @Mock
    private KandidatService service;

    @Mock
    private TokenUtils tokenUtils;

    @Before
    public void setUp() {
        controller = new PersonbrukersTilretteleggingsbehovController(service, tokenUtils);
    }

    @Test
    public void hentTilretteleggingsbehov__skal_returnere_mine_tilretteleggingsbehov() {
        Kandidat kandidat = enKandidat();

        when(tokenUtils.hentInnloggetBrukersFødselsnummer()).thenReturn(enKandidat().getFnr());
        when(service.hentAktørId(kandidat.getFnr())).thenReturn(kandidat.getAktørId());
        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentTilretteleggingsbehov();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }
}
