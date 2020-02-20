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

    @Mock
    private VeilarbOppfolgingClient veilarbOppfolgingClient;

    @Before
    public void setUp() {
        controller = new PersonbrukersTilretteleggingsbehovController(service, tokenUtils, veilarbOppfolgingClient);
    }

    @Test
    public void hentTilretteleggingsbehov__skal_returnere_mine_tilretteleggingsbehov() {
        Kandidat kandidat = enKandidat();

        when(tokenUtils.hentInnloggetBruker()).thenReturn(enKandidat().getFnr());
        when(service.hentAktørId(kandidat.getFnr())).thenReturn(kandidat.getAktørId());
        when(service.hentNyesteKandidat(kandidat.getAktørId())).thenReturn(Optional.of(kandidat));

        ResponseEntity<Kandidat> respons = controller.hentTilretteleggingsbehov();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(kandidat);
    }

    @Test
    public void hentOppfølgingsstatus__skal_returnere_oppfølgingsstatus() {
        Oppfølgingsstatus oppfølgingsstatus = Oppfølgingsstatus.builder().underOppfolging(true).build();
        when(veilarbOppfolgingClient.hentOppfølgingsstatus()).thenReturn(oppfølgingsstatus);

        ResponseEntity<Oppfølgingsstatus> respons = controller.hentOppfølgingsstatus();

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isEqualTo(oppfølgingsstatus);
    }
}
