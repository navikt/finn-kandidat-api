package no.nav.finnkandidatapi;

import no.nav.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.finnkandidatapi.aktørregister.Identinfo;
import no.nav.finnkandidatapi.aktørregister.IdentinfoForAktør;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.sts.STSClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static no.nav.finnkandidatapi.TestData.etStsToken;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktørRegisterClientTest {

    private AktørRegisterClient aktørRegisterClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private STSClient stsClient;

    private String fnr = "12312312312";

    @Before
    public void setUp() {
        aktørRegisterClient = new AktørRegisterClient(restTemplate, "http://www.url.com", stsClient);
    }

    @Test(expected = FinnKandidatException.class)
    public void validerRespons__skal_kaste_exception_hvis_ingen_identinfo() {
        IdentinfoForAktør responsUtenAktørId = new IdentinfoForAktør(Collections.emptyList(), null);
        mockKallTilAktørRegister(responsUtenAktørId);
        aktørRegisterClient.tilAktørId(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void validerRespons__skal_kaste_exception_hvis_aktørregister_returnerer_med_feilmelding() {
        IdentinfoForAktør responsMedFeilmelding = new IdentinfoForAktør(Collections.singletonList((new Identinfo("", "", true))), "feil");
        mockKallTilAktørRegister(responsMedFeilmelding);
        aktørRegisterClient.tilAktørId(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void validerRespons__skal_kaste_exception_hvis_aktørregister_returnerer_flere_identer() {
        IdentinfoForAktør responsMedFlereFnr = new IdentinfoForAktør(Arrays.asList(new Identinfo("", "", true), new Identinfo("", "", true)), null);
        mockKallTilAktørRegister(responsMedFlereFnr);
        aktørRegisterClient.tilAktørId(fnr);
    }

    private void mockKallTilAktørRegister(IdentinfoForAktør identinfoForAktør) {
        ResponseEntity<Map<String, IdentinfoForAktør>> respons = ResponseEntity.ok(Map.of(fnr, identinfoForAktør));

        when(stsClient.hentSTSToken()).thenReturn(etStsToken());
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, IdentinfoForAktør>>() {})))
                .thenReturn(respons);
    }
}
