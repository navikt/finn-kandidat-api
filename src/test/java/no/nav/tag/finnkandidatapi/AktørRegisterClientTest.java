package no.nav.tag.finnkandidatapi;

import no.nav.tag.finnkandidatapi.aktørregister.AktørRegisterClient;
import no.nav.tag.finnkandidatapi.aktørregister.Identinfo;
import no.nav.tag.finnkandidatapi.aktørregister.IdentinfoForAktør;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.sts.STSClient;
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

import static no.nav.tag.finnkandidatapi.TestData.etStsToken;
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
        IdentinfoForAktør responsUtenAktorId = new IdentinfoForAktør(Collections.emptyList(), null);
        mockKallTilAktørRegister(responsUtenAktorId);
        aktørRegisterClient.tilAktorId(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void validerRespons__skal_kaste_exception_hvis_aktørregister_returnerer_med_feilmelding() {
        IdentinfoForAktør responsMedFeilmelding = new IdentinfoForAktør(Collections.singletonList((new Identinfo("", "", true))), "feil");
        mockKallTilAktørRegister(responsMedFeilmelding);
        aktørRegisterClient.tilAktorId(fnr);
    }

    @Test(expected = FinnKandidatException.class)
    public void validerRespons__skal_kaste_exception_hvis_aktørregister_returnerer_flere_identer() {
        IdentinfoForAktør responsMedFlereFnr = new IdentinfoForAktør(Arrays.asList(new Identinfo("", "", true), new Identinfo("", "", true)), null);
        mockKallTilAktørRegister(responsMedFlereFnr);
        aktørRegisterClient.tilAktorId(fnr);
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
