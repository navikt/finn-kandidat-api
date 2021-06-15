package no.nav.finnkandidatapi.synlighet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.finnkandidatapi.sts.STSClient;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
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
    private final AktorOppslagClient aktorOppslagClient;

    public SynlighetController(
            TilgangskontrollService tilgangskontroll,
            @Value("${arbeidssoker.url}") String arbeidssokerUrl,
            STSClient stsClient,
            AktorOppslagClient aktorOppslagClient
    ) {
        this.tilgangskontroll = tilgangskontroll;
        this.arbeidssokerUrl = arbeidssokerUrl;
        this.stsClient = stsClient;
        this.aktorOppslagClient = aktorOppslagClient;
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<?> harCvOgJobbønsker(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt();
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);

        Fnr fnr = aktorOppslagClient.hentFnr(new AktorId(aktørId));

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<ArbeidssøkerResponse> response = restTemplate.exchange(
                    arbeidssokerUrl + "/rest/v2/arbeidssoker/" + fnr.get() + "?erManuell=false",
                    HttpMethod.GET,
                    bearerToken(),
                    ArbeidssøkerResponse.class
            );

            return HarCvOgJobbønskerResponse.fra(response.getBody());

        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return fraFeilmelding(exception.getResponseBodyAsString());

            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(exception.getResponseBodyAsString());
            }
        }
    }

    private ResponseEntity<?> fraFeilmelding(String body) {
        if (!body.contains("CV finnes ikke")) {
            val feilmelding = "Uventet respons i kall mot arbeidssoker: " + body;
            log.error(feilmelding);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(feilmelding);
        }
        return HarCvOgJobbønskerResponse.manglerCv();
    }

    private HttpEntity<?> bearerToken() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + stsClient.hentSTSToken().getAccessToken());
        log.info("Header cv " + headers);
        return new HttpEntity<>(headers);
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

    private void loggBrukAvEndepunkt() {
        log.info(
                "Bruker med ident {} kaller endepunktet synlighet.",
                tilgangskontroll.hentInnloggetVeileder().getNavIdent()
        );
    }
}
