package no.nav.tag.finnkandidatapi.veilarbarena;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
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
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;
    private final String veilarbarenaUrl;

    public VeilarbArenaClient(TokenUtils tokenUtils, RestTemplate restTemplate, @Value("${veilarbarena.url}") String veilarbarenaUrl) {
        this.tokenUtils = tokenUtils;
        this.restTemplate = restTemplate;
        this.veilarbarenaUrl = veilarbarenaUrl;
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

        // TODO: Cookie eller bearer auth?
        headers.setBearerAuth(tokenUtils.getTokenForInnloggetBruker().getIdToken());
        return new HttpEntity<>(headers);
    }
}
