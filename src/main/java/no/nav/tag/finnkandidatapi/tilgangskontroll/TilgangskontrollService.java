package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@Slf4j
@Service
public class TilgangskontrollService {
    private final Supplier<String> oidcUserTokenSupplier;
    private final RestTemplate restTemplate;
    private final Supplier<String> oidcSystemUserTokenSupplier;
    @Value("${veilarbabac.url}") private String veilarbabacUrl;

    public TilgangskontrollService(
            Supplier<String> oidcUserTokenSupplier,
            RestTemplate restTemplate,
            Supplier<String> oidcSystemUserTokenSupplier) {
        this.oidcUserTokenSupplier = oidcUserTokenSupplier;
        this.restTemplate = restTemplate;
        this.oidcSystemUserTokenSupplier = oidcSystemUserTokenSupplier;
    }

    public boolean harTilgang(String fnr) {
        String uriString = UriComponentsBuilder.fromHttpUrl(veilarbabacUrl)
                .path("/person")
                .queryParam("fnr", fnr)
                .queryParam("action", "update")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", oidcUserTokenSupplier.get());
        headers.set("AUTHORIZATION", oidcSystemUserTokenSupplier.get());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> jsonResponse = restTemplate.exchange(
                uriString,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        log.info("veilarbabac-response: " + jsonResponse.getBody());

        return true;
    }
}
