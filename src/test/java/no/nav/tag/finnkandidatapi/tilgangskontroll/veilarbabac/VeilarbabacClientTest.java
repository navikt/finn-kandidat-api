package no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac;

import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSClient;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STStoken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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

    @Before
    public void setUp() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_gjøre_kall_med_riktige_parametre() {
        STStoken stsToken = new STStoken("asdfasdgdf", "", 0);
        String oidcToken = "sdgsfdhgsdfd";
        String fnr = "43632521";

        when(stsClient.getToken()).thenReturn(stsToken);

        when(tokenUtils.hentInnloggetOidcToken()).thenReturn(oidcToken);

        VeilarbabacClient veilarbabacClient = new VeilarbabacClient(
                tokenUtils,
                restTemplate,
                stsClient,
                "https://test.no"
        );

        veilarbabacClient.harSkrivetilgangTilKandidat(fnr);

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", oidcToken);
        headers.set("AUTHORIZATION", "Bearer " + stsToken.getAccess_token());
        headers.setContentType(MediaType.APPLICATION_JSON);

        verify(restTemplate).exchange(
                eq("https://test.no/person?fnr=" + fnr + "&action=update"),
                eq(HttpMethod.GET),
                eq(new HttpEntity(headers)),
                eq(String.class)
        );

    }
}