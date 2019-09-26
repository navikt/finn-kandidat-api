package no.nav.tag.finnkandidatapi.veilarbarena;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class VeilarbArenaClient {
    private final RestTemplate restTemplate;
    private final String veilarbarenaUrl;
    private final STSClient stsClient;

    public VeilarbArenaClient(RestTemplate restTemplate, @Value("${veilarbarena.url}") String veilarbarenaUrl, STSClient stsClient) {
        this.restTemplate = restTemplate;
        this.veilarbarenaUrl = veilarbarenaUrl;
        this.stsClient = stsClient;
    }

    public Personinfo hentPersoninfo(String fnr) {
        String uri = UriComponentsBuilder.fromHttpUrl(veilarbarenaUrl)
                .path("/oppfolgingsbruker/" + fnr)
                .toUriString();

        ResponseEntity<Personinfo> respons = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpHeadere(),
                Personinfo.class
        );

        // TODO: Valider respons?
        return respons.getBody();
    }

    private HttpEntity httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hentOidcTokenTilSystembruker());
        return new HttpEntity<>(headers);
    }

    private String hentOidcTokenTilSystembruker() {
        return stsClient.hentSTSToken().getAccessToken();
    }
}
