package no.nav.finnkandidatapi.veilarbarena;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.Kandidat;
import no.nav.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static no.nav.finnkandidatapi.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbArenaClientTest {
    private VeilarbArenaClient veilarbArenaClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenUtils tokenUtils;

    @Before
    public void setUp() {
        veilarbArenaClient = new VeilarbArenaClient(
                restTemplate,
                "https://www.eksempel.no",
                tokenUtils,
                mock(MeterRegistry.class, RETURNS_DEEP_STUBS)
        );
    }

    @Test
    public void hentOppfølgingsbruker__skal_hente_oppfølgingsbruker() {
        Kandidat kandidat = enKandidat();
        Oppfølgingsbruker oppfølgingsbruker = enOppfølgingsbruker();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Oppfølgingsbruker.class)))
                .thenReturn(new ResponseEntity<>(oppfølgingsbruker, HttpStatus.OK));
        when(tokenUtils.hentOidcToken()).thenReturn("123");

        Oppfølgingsbruker hentetOppfølgingsbruker = veilarbArenaClient.hentOppfølgingsbruker(kandidat.getFnr(), kandidat.getAktørId());

        assertThat(hentetOppfølgingsbruker).isEqualTo(oppfølgingsbruker);
    }

    @Test(expected = FinnKandidatException.class)
    public void hentOppfølgingsbruker__skal_kaste_FinnKandidatException_hvis_feil() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Oppfølgingsbruker.class)))
                .thenThrow(RestClientResponseException.class);
        veilarbArenaClient.hentOppfølgingsbruker(etFnr(), enAktørId());
    }

    @Test
    public void hentOppfølgingsbruker__skal_returnere_Oppfølgingsbruker_med_navkontor_lik_null_hvis_no_content_fra_veilarbarena() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Oppfølgingsbruker.class)))
                .thenReturn(ResponseEntity.noContent().build());
        when(tokenUtils.hentOidcToken()).thenReturn("123");

        Oppfølgingsbruker oppfølgingsbruker = veilarbArenaClient.hentOppfølgingsbruker(etFnr(), enAktørId());

        assertThat(oppfølgingsbruker.getNavKontor()).isNull();
    }
}
