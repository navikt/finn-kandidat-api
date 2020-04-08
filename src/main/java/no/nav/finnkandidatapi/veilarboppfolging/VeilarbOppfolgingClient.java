package no.nav.finnkandidatapi.veilarboppfolging;

import no.nav.finnkandidatapi.sts.STSClient;
import no.nav.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class VeilarbOppfolgingClient {

    private final RestTemplate restTemplate;
    private final TokenUtils tokenUtils;
    private final String url;
    private final STSClient stsClient;

    public VeilarbOppfolgingClient(
            RestTemplate restTemplate,
            TokenUtils tokenUtils,
            @Value("${veilarboppfolging.url}") String url,
            STSClient stsClient
    ) {
        this.restTemplate = restTemplate;
        this.tokenUtils = tokenUtils;
        this.url = url;
        this.stsClient = stsClient;
    }

    public Oppfølgingsstatus hentOppfølgingsstatus(String fnr) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .path("/underoppfolging")
                .queryParam("fnr", fnr)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Oppfølgingsstatus> respons = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Oppfølgingsstatus.class);
        return respons.getBody();
    }

    public Oppfølgingsstatus hentOppfølgingsstatus() {
        URI uri = URI.create(url + "/underoppfolging");
        ResponseEntity<Oppfølgingsstatus> respons = restTemplate.exchange(uri, HttpMethod.GET, httpEntity(), Oppfølgingsstatus.class);
        return respons.getBody();
    }

    private HttpEntity<String> httpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtils.hentOidcTokenSelvbetjening());
        return new HttpEntity<>(headers);
    }
}
