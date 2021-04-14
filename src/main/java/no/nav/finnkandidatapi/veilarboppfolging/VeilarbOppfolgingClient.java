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

import java.net.URI;

@Component
public class VeilarbOppfolgingClient {

    private final RestTemplate restTemplate;
    private final TokenUtils tokenUtils;
    private final String url;

    public VeilarbOppfolgingClient(
            RestTemplate restTemplate,
            TokenUtils tokenUtils,
            @Value("${veilarboppfolging.url}") String url
    ) {
        this.restTemplate = restTemplate;
        this.tokenUtils = tokenUtils;
        this.url = url;
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
