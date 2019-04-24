package no.nav.tag.finnkandidatapi.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Slf4j
public class TilgangskontrollService {
    private final Supplier<String> oidcTokenSupplier;
    private final RestTemplate restTemplate;

    public TilgangskontrollService(Supplier<String> oidcTokenSupplier, RestTemplate restTemplate) {
        this.oidcTokenSupplier = oidcTokenSupplier;
        this.restTemplate = restTemplate;
    }

    public boolean harTilgang() {

        String VEILARBABAC_PATH = "https://veilarbabac-q1.nais.preprod.local";

        String fnr = "12345678910";

        String path = VEILARBABAC_PATH
                + "/person"
                + "?fnr=" + fnr
                + "&action=update";

        // Header: AUTHORIZATION. Bearer-token med systembruker
        // Header: subject, oidc-token til innlogget bruker.

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", oidcTokenSupplier.get());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> jsonResponse = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        log.info("veilarbabac-response: " + jsonResponse.getBody());

        return true;
    }
}
