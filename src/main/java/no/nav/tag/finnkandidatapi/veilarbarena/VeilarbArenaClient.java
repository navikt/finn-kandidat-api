package no.nav.tag.finnkandidatapi.veilarbarena;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class VeilarbArenaClient {
    private final RestTemplate restTemplate;
    private final String veilarbarenaUrl;

    public VeilarbArenaClient(RestTemplate restTemplate, @Value("${veilarbarena.url}") String veilarbarenaUrl) {
        this.restTemplate = restTemplate;
        this.veilarbarenaUrl = veilarbarenaUrl;
    }

    public Personinfo hentPersoninfo(String fnr) {
        String uri = UriComponentsBuilder.fromHttpUrl(veilarbarenaUrl)
                .path("/oppfolgingsbruker/" + fnr)
                .toUriString();

        ResponseEntity<Personinfo> respons = restTemplate.getForEntity(uri, Personinfo.class);

        // TODO: Valider respons?
        return respons.getBody();
    }
}
