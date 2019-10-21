package no.nav.tag.finnkandidatapi.veilarbarena;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class VeilarbArenaClient {

    private final RestTemplate restTemplate;
    private final String veilarbarenaUrl;
    private final TokenUtils tokenUtils;

    public VeilarbArenaClient(
            RestTemplate restTemplate,
            @Value("${veilarbarena.url}") String veilarbarenaUrl,
            TokenUtils tokenUtils
    ) {
        this.restTemplate = restTemplate;
        this.veilarbarenaUrl = veilarbarenaUrl;
        this.tokenUtils = tokenUtils;
    }

    public Oppfølgingsbruker hentPersoninfo(String fnr) {
        String uri = UriComponentsBuilder.fromHttpUrl(veilarbarenaUrl)
                .path("/oppfolgingsbruker/" + fnr)
                .toUriString();

        try {
            ResponseEntity<Oppfølgingsbruker> respons = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpHeadere(),
                    Oppfølgingsbruker.class
            );
            return respons.getBody();

        } catch (RestClientResponseException exception) {
            log.error("Kunne ikke hente personinfo fra veilarbarena", exception);
            throw new FinnKandidatException("Kunne ikke hente personinfo fra veilarbarena");
        }
    }

    private HttpEntity httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtils.hentOidcToken());
        return new HttpEntity<>(headers);
    }
}
