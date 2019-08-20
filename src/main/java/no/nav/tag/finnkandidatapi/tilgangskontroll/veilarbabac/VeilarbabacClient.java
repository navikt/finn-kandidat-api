package no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollAction;
import no.nav.tag.finnkandidatapi.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class VeilarbabacClient {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final String veilarbabacUrl;


    public static final String PERMIT_RESPONSE = "permit";
    public static final String DENY_RESPONSE = "deny";

    public VeilarbabacClient(
            RestTemplate restTemplate,
            STSClient stsClient,
            @Value("${veilarbabac.url}") String veilarbabacUrl
    ) {
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
        this.veilarbabacUrl = veilarbabacUrl;
    }

    public boolean sjekkTilgangAktørId(Veileder veileder, String id, TilgangskontrollAction action) {
        return sjekkTilgang(veileder, id, action, "aktorId");
    }

    private boolean sjekkTilgang(Veileder veileder, String id, TilgangskontrollAction action, String idType) {
        String response = hentTilgang(veileder, id, action, idType);

        if (PERMIT_RESPONSE.equals(response)) {
            return true;
        } else if (DENY_RESPONSE.equals(response)) {
            return false;
        }

        throw new FinnKandidatException("Ukjent respons fra veilarbabac: " + response);
    }

    private String hentTilgang(Veileder veileder, String id, TilgangskontrollAction action, String idType) {
        String uriString = UriComponentsBuilder.fromHttpUrl(veilarbabacUrl)
                .path("/person")
                .queryParam(idType, id)
                .queryParam("action", action.toString())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", veileder.getNavIdent());
        headers.set("subjectType", "InternBruker");
        headers.setBearerAuth(hentOidcTokenTilSystembruker());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                uriString,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    private String hentOidcTokenTilSystembruker() {
        return stsClient.hentSTSToken().getAccessToken();
    }
}
