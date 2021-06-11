package no.nav.finnkandidatapi.synlighet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.finnkandidatapi.sts.STSClient;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_OPENAM;

@Slf4j
@ProtectedWithClaims(issuer = ISSUER_OPENAM)
@RestController
@RequestMapping("/synlighet")
public class SynlighetController {

    private final TilgangskontrollService tilgangskontroll;
    private final String arbeidssokerUrl;
    private final STSClient stsClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public SynlighetController(TilgangskontrollService tilgangskontroll, @Value("arbeidssoker.url") String arbeidssokerUrl,
                               STSClient stsClient) {
        this.tilgangskontroll = tilgangskontroll;
        this.arbeidssokerUrl = arbeidssokerUrl;
        this.stsClient = stsClient;
    }

    @GetMapping(value = "/{aktørId}")
    public ResponseEntity<?> harCvOgJobbønsker(@PathVariable("aktørId") String aktørId) throws JsonProcessingException {
        loggBrukAvEndepunkt("synlighet");
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(arbeidssokerUrl, HttpMethod.GET, bearerToken(), String.class);
        if(response.getStatusCode() == HttpStatus.NOT_FOUND)
            return fraFeilmelding(response.getBody());

        ArbeidssøkerResponse arbeidssøkerResponse = objectMapper.readValue(response.getBody(),ArbeidssøkerResponse.class);
        return HarCvOgJobbønskerResponse.fra(arbeidssøkerResponse);
    }

    private HttpEntity<?> bearerToken() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(headers);
    }

    private ResponseEntity<?> fraFeilmelding(String body) {
        if(!body.contains("CV finnes ikke")) {
            val feilmelding = "Uventet respons i kall mot arbeidssoker: " + body;
            log.error(feilmelding);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(feilmelding);
        }
        return HarCvOgJobbønskerResponse.manglerCv();
    }

    @RequiredArgsConstructor
    private static class HarCvOgJobbønskerResponse {
        private final boolean harCv;
        private final boolean harJobbprofil;

        static ResponseEntity<HarCvOgJobbønskerResponse> manglerCv() {
            return harCvOgJobbønskerResponse(false, false);
        }

        public static ResponseEntity<HarCvOgJobbønskerResponse> fra(ArbeidssøkerResponse arbeidssøkerResponse) {
            return harCvOgJobbønskerResponse(true, arbeidssøkerResponse.jobbprofil != null);
        }

        private static ResponseEntity<HarCvOgJobbønskerResponse> harCvOgJobbønskerResponse(boolean harCv, boolean harJobbprofil) {
            return response(new HarCvOgJobbønskerResponse(harCv, harJobbprofil));
        }

        private static ResponseEntity<HarCvOgJobbønskerResponse> response(HarCvOgJobbønskerResponse response) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    private static class ArbeidssøkerResponse {
        Jobbprofil jobbprofil;
    }
    private static class Jobbprofil {

    }

    private void loggBrukAvEndepunkt(String endepunkt) {
        log.info(
                "Bruker med ident {} kaller endepunktet {}.",
                tilgangskontroll.hentInnloggetVeileder().getNavIdent(),
                endepunkt
        );
    }
}
