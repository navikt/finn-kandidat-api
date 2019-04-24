package no.nav.tag.finnkandidatapi.tilgangskontroll.sts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class STSClient {

    private final RestTemplate stsBasicAuthRestTemplate;

    @Value("${sts.url}") private String stsUrl;

    public STSClient(RestTemplate stsBasicAuthRestTemplate) {
        this.stsBasicAuthRestTemplate = stsBasicAuthRestTemplate;
    }

    public STStoken getToken() {
        try {
            ResponseEntity<STStoken> response = buildUriAndExecuteRequest();
            if(response.getStatusCode() != HttpStatus.OK){
                String message = "Kall mot STS feiler med HTTP-" + response.getStatusCode();
                log.error(message);
                throw new RuntimeException(message);
            }
            return response.getBody();
        }
        catch(HttpClientErrorException e){
            log.error("Feil ved oppslag i STS", e);
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<STStoken> buildUriAndExecuteRequest(){

        String uriString = UriComponentsBuilder.fromHttpUrl(stsUrl + "/sts/token")
                .queryParam("grant_type","client_credentials")
                .queryParam("scope","openid")
                .toUriString();

        return stsBasicAuthRestTemplate.exchange(
                uriString,
                HttpMethod.GET,
                getRequestEntity(),
                STStoken.class
        );
    }

    private HttpEntity<String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }

}
