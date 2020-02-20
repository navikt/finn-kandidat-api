package no.nav.finnkandidatapi.tilgangskontroll.veilarbabac;

import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.kandidat.Veileder;
import no.nav.finnkandidatapi.sts.STSClient;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static no.nav.finnkandidatapi.config.CacheConfig.ABAC_CACHE;

@Slf4j
@Service
public class VeilarbabacClient {

    public static final String PERMIT_RESPONSE = "permit";
    public static final String DENY_RESPONSE = "deny";

    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final String veilarbabacUrl;

    public VeilarbabacClient(
            RestTemplate restTemplate,
            STSClient stsClient,
            @Value("${veilarbabac.url}") String veilarbabacUrl
    ) {
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
        this.veilarbabacUrl = veilarbabacUrl;
    }

    @Cacheable(ABAC_CACHE)
    public boolean sjekkTilgang(Veileder veileder, String aktørId, TilgangskontrollAction action) {
        String response;
        try {
            response = hentTilgang(veileder, aktørId, action);
        } catch (HttpClientErrorException e) {
            stsClient.evictToken();
            response = hentTilgang(veileder, aktørId, action);
        }

        if (PERMIT_RESPONSE.equals(response)) {
            return true;
        } else if (DENY_RESPONSE.equals(response)) {
            return false;
        }

        throw new FinnKandidatException("Ukjent respons fra veilarbabac: " + response);
    }

    private String hentTilgang(Veileder veileder, String aktørId, TilgangskontrollAction action) {
        String uriString = UriComponentsBuilder.fromHttpUrl(veilarbabacUrl)
                .path("/person")
                .queryParam("aktorId", aktørId)
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
