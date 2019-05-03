package no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TokenUtils;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class VeilarbabacClient {
    private final TokenUtils tokenUtils;
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final String veilarbabacUrl;


    public static final String PERMIT_RESPONSE = "permit";
    public static final String DENY_RESPONSE = "deny";

    public VeilarbabacClient(
            TokenUtils tokenUtils,
            RestTemplate restTemplate,
            STSClient stsClient,
            @Value("${veilarbabac.url}") String veilarbabacUrl
    ) {
        this.tokenUtils = tokenUtils;
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
        this.veilarbabacUrl = veilarbabacUrl;
    }

    public boolean harSkrivetilgangTilKandidat(String fnr) {
        String response;

        try {
            response = hentTilgangFraVeilarbAbac(fnr);
        } catch(HttpClientErrorException e) {
            log.error("Feil ved kall til veilarbabac", e);
            throw e;
        }

        if (PERMIT_RESPONSE.equals(response)) {
            return true;
        }

        if (DENY_RESPONSE.equals(response)) {
            return false;
        }

        throw new FinnKandidatException("Ukjent respons fra veilarbabac: " + response);
    }

    private String hentTilgangFraVeilarbAbac(String fnr) {
        String uriString = UriComponentsBuilder.fromHttpUrl(veilarbabacUrl)
                .path("/person")
                .queryParam("fnr", fnr)
                .queryParam("action", "update")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", tokenUtils.hentInnloggetOidcToken());
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
