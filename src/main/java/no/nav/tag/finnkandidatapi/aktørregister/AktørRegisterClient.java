package no.nav.tag.finnkandidatapi.aktørregister;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class AktørRegisterClient {

    private final RestTemplate restTemplate;

    @Value("${aktørregister.url}")
    private final String aktørRegisterUrl;

    private final STSClient stsClient;

    public AktørRegisterClient(String aktørRegisterUrl, STSClient stsClient) {
        this.restTemplate = new RestTemplate();
        this.aktørRegisterUrl = aktørRegisterUrl;
        this.stsClient = stsClient;
    }

    public String tilFnr(String aktørId) {
        String uri = UriComponentsBuilder.fromHttpUrl(aktørRegisterUrl)
                .queryParam("identgruppe", "NorskIdent")
                .queryParam("gjeldende", true)
                .toUriString();

        ResponseEntity<Map<String, IdentinfoForAktør>> respons = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpHeadere(aktørId),
                new ParameterizedTypeReference<>() {}
        );

        validerRespons(aktørId, respons);

        IdentinfoForAktør identinfoForAktør = respons.getBody().get(aktørId);
        return hentGjeldendeFnr(identinfoForAktør);
    }

    private String hentGjeldendeFnr(IdentinfoForAktør identinfoForAktør) {
        return identinfoForAktør.getIdenter().get(0).getIdent();
    }

    private HttpEntity httpHeadere(String aktørId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        headers.set("Nav-Call-Id", UUID.randomUUID().toString());
        headers.set("Nav-Consumer-Id", "srvfinn-kandidat-api");
        headers.set("Nav-Personidenter", aktørId);
        return new HttpEntity<>(headers);
    }

    private void validerRespons(String aktørId, ResponseEntity<Map<String, IdentinfoForAktør>> respons) {
        if (!respons.getStatusCode().equals(HttpStatus.OK)) {
            throw new FinnKandidatException("Feil fra aktørregister, HTTP-status: " + respons.getStatusCodeValue());
        }

        IdentinfoForAktør identinfoForAktør = respons.getBody().get(aktørId);
        if (identinfoForAktør == null) {
            throw new FinnKandidatException("Fant ingen identinfo for aktørId: " + aktørId);
        }

        if (identinfoForAktør.getFeilmelding() != null) {
            throw new FinnKandidatException("Feil fra aktørregister for aktørId " + aktørId + ", feilmelding: " + identinfoForAktør.getFeilmelding());
        }

        if (identinfoForAktør.getIdenter().size() > 1) {
            throw new FinnKandidatException("Forventet 1 fnr for aktørId" + aktørId + ", fant " + identinfoForAktør.getIdenter().size());
        }
    }
}
