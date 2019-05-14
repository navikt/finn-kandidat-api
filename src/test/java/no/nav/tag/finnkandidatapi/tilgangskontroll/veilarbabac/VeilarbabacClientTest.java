package no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac;

import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollAction;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSClient;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSToken;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static no.nav.tag.finnkandidatapi.TestData.enVeileder;
import static no.nav.tag.finnkandidatapi.TestData.etStsToken;
import static no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient.DENY_RESPONSE;
import static no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient.PERMIT_RESPONSE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbabacClientTest {

    @Mock
    private TokenUtils tokenUtils;

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
                tokenUtils,
                restTemplate,
                stsClient,
                "https://test.no"
        );
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_false_hvis_deny() {
        mockReturverdiFraVeilarbabac(DENY_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang(enVeileder(), "12345678910", TilgangskontrollAction.update)).isFalse();
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_true_hvis_permit() {
        mockReturverdiFraVeilarbabac(PERMIT_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang(enVeileder(), "12345678910", TilgangskontrollAction.update)).isTrue();
    }

    @Test(expected = FinnKandidatException.class)
    public void harSkrivetilgangTilKandidat__skal_kaste_exception_hvis_ikke_allow_eller_deny() {
        mockReturverdiFraVeilarbabac("blabla");
        veilarbabacClient.sjekkTilgang(enVeileder(), "12345678910", TilgangskontrollAction.update);
    }

    @Ignore // TODO Oppdater denne testen
    @Test
    public void harSkrivetilgangTilKandidat__skal_gjøre_kall_med_riktige_parametre() {
        STSToken stsToken = etStsToken();
        String oidcToken = "sdgsfdhgsdfd";
        String fnr = "12345678910";

        when(stsClient.hentSTSToken()).thenReturn(stsToken);

        when(tokenUtils.hentInnloggetOidcToken()).thenReturn(oidcToken);

        veilarbabacClient.sjekkTilgang(enVeileder(), fnr, TilgangskontrollAction.update);

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", oidcToken);
        headers.set("AUTHORIZATION", "Bearer " + stsToken.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        verify(restTemplate).exchange(
                eq("https://test.no/person?fnr=" + fnr + "&action=update"),
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