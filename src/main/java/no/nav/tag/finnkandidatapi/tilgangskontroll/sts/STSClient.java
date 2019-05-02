package no.nav.tag.finnkandidatapi.tilgangskontroll.sts;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class STSClient {

    private final RestTemplate stsBasicAuthRestTemplate;
    private final String stsUrl;

    public STSClient(
            RestTemplate stsBasicAuthRestTemplate,
            @Value("${sts.url}") String stsUrl
    ) {
        this.stsBasicAuthRestTemplate = stsBasicAuthRestTemplate;
        this.stsUrl = stsUrl;
    }

    public STSToken hentSTSToken() {
        try {
            return hentToken();
        } catch(HttpClientErrorException e) {
            log.error("Feil ved oppslag i STS", e);
            throw e;
        }
    }

    private STSToken hentToken() {
        String uriString = UriComponentsBuilder.fromHttpUrl(stsUrl + "/sts/token")
                .queryParam("grant_type","client_credentials")
                .queryParam("scope","openid")
                .toUriString();

        return stsBasicAuthRestTemplate.exchange(
                uriString,
                HttpMethod.GET,
                getRequestEntity(),
                STSToken.class
        ).getBody();
    }

    private HttpEntity<String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }

}
