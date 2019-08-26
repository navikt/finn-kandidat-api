package no.nav.tag.finnkandidatapi.aktørregister;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AktørRegisterClient {

    private final RestTemplate restTemplate;
    private final String aktørRegisterUrl;
    private final STSClient stsClient;

    public AktørRegisterClient(RestTemplate restTemplate, @Value("${aktørregister.url}") String aktørRegisterUrl, STSClient stsClient) {
        this.restTemplate = restTemplate;
        this.aktørRegisterUrl = aktørRegisterUrl;
        this.stsClient = stsClient;
    }

    public String tilFnr(String aktørId) {
        return konverterId(aktørId,  "NorskIdent");
    }

    public String tilAktørId(String fnr) {
        return konverterId(fnr,  "AktoerId");
    }

    private String konverterId(String fraId, String type) {
        String uri = UriComponentsBuilder.fromHttpUrl(aktørRegisterUrl)
                .path("/identer")
                .queryParam("identgruppe", type)
                .queryParam("gjeldende", true)
                .toUriString();

        ResponseEntity<Map<String, IdentinfoForAktør>> respons = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpHeadere(fraId),
                new ParameterizedTypeReference<Map<String, IdentinfoForAktør>>() {}
        );

        IdentinfoForAktør identinfoForAktør = respons.getBody().get(fraId);
        validerRespons(fraId, identinfoForAktør, type);
        return hentGjeldendeId(identinfoForAktør);
    }

    private String hentGjeldendeId(IdentinfoForAktør identinfoForAktør) {
        return identinfoForAktør.getIdenter().get(0).getIdent();
    }

    private HttpEntity httpHeadere(String aktørId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        headers.set("Nav-Call-Id", UUID.randomUUID().toString());
        headers.set("Nav-Consumer-Id", "srvfinn-kandidat-api");
        headers.set("Nav-Personidenter", aktørId);
        return new HttpEntity<>(headers);
    }

    private void validerRespons(String id, IdentinfoForAktør identinfoForAktør, String type) {
        if (identinfoForAktør == null) {
            throw new FinnKandidatException("Fant ingen identinfo for id: " + id + " type " + type);
        }

        if (identinfoForAktør.getFeilmelding() != null) {
            throw new FinnKandidatException("Feil fra aktørregister for id " + id + " type " + type + ", feilmelding: " + identinfoForAktør.getFeilmelding());
        }

        if (identinfoForAktør.getIdenter().size() != 1) {
            throw new FinnKandidatException("Forventet 1 fnr for id" + id + " type " + type + ", fant " + identinfoForAktør.getIdenter().size());
        }
    }

}
