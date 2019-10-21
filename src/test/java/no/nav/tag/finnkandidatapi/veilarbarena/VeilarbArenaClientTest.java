package no.nav.tag.finnkandidatapi.veilarbarena;

import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.kandidat.Kandidat;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static no.nav.tag.finnkandidatapi.TestData.enKandidat;
import static no.nav.tag.finnkandidatapi.TestData.personinfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbArenaClientTest {
    private VeilarbArenaClient veilarbArenaClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenUtils tokenUtils;

    @Before
    public void setUp() {
        veilarbArenaClient = new VeilarbArenaClient(restTemplate, "https://www.eksempel.no", tokenUtils);
    }

    @Test
    public void hentPersonInfo__skal_hente_personinfo() {
        Kandidat kandidat = enKandidat();
        Oppfølgingsbruker oppfølgingsbruker = personinfo();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Oppfølgingsbruker.class)))
                .thenReturn(new ResponseEntity<>(oppfølgingsbruker, HttpStatus.OK));
        when(tokenUtils.hentOidcToken()).thenReturn("123");

        Oppfølgingsbruker hentetOppfølgingsbruker = veilarbArenaClient.hentPersoninfo(kandidat.getFnr());

        assertThat(hentetOppfølgingsbruker).isEqualTo(oppfølgingsbruker);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentPersonInfo__skal_kaste_FinnKandidatException_hvis_feil() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Oppfølgingsbruker.class)))
                .thenThrow(RestClientResponseException.class);
        veilarbArenaClient.hentPersoninfo("123");
    }
}
