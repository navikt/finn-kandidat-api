package no.nav.finnkandidatapi.tilgangskontroll.veilarbabac;

import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.sts.STSClient;
import no.nav.finnkandidatapi.sts.STSToken;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static no.nav.finnkandidatapi.TestData.enVeileder;
import static no.nav.finnkandidatapi.TestData.etStsToken;
import static no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient.DENY_RESPONSE;
import static no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient.PERMIT_RESPONSE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbabacClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private STSClient stsClient;

    private VeilarbabacClient veilarbabacClient;

    @Before
    public void setUp() {
        mockReturverdiFraVeilarbabac("permit");
        when(stsClient.hentSTSToken()).thenReturn(etStsToken());
        veilarbabacClient = new VeilarbabacClient(
                restTemplate,
                stsClient,
                "https://test.no"
        );
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_false_hvis_deny() {
        mockReturverdiFraVeilarbabac(DENY_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang(enVeileder(), "1000000000001", TilgangskontrollAction.update)).isFalse();
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_true_hvis_permit() {
        mockReturverdiFraVeilarbabac(PERMIT_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang(enVeileder(), "1000000000001", TilgangskontrollAction.update)).isTrue();
    }

    @Test(expected = FinnKandidatException.class)
    public void harSkrivetilgangTilKandidat__skal_kaste_exception_hvis_ikke_allow_eller_deny() {
        mockReturverdiFraVeilarbabac("blabla");
        veilarbabacClient.sjekkTilgang(enVeileder(), "1000000000001", TilgangskontrollAction.update);
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_gjøre_kall_med_riktige_parametre() {
        STSToken stsToken = etStsToken();
        String aktørId = "1000000000001";

        Veileder veileder = enVeileder();

        when(stsClient.hentSTSToken()).thenReturn(stsToken);

        veilarbabacClient.sjekkTilgang(enVeileder(), aktørId, TilgangskontrollAction.update);

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", veileder.getNavIdent());
        headers.set("subjectType", "InternBruker");
        headers.set("Authorization", "Bearer " + stsToken.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        verify(restTemplate).exchange(
                eq("https://test.no/person?aktorId=" + aktørId + "&action=update"),
                eq(HttpMethod.GET),
                eq(new HttpEntity(headers)),
                eq(String.class)
        );
    }

    private void mockReturverdiFraVeilarbabac(String response) {
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body(response));
    }
}
