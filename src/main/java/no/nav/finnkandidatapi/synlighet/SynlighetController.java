package no.nav.finnkandidatapi.synlighet;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.finnkandidatapi.tilgangskontroll.TokenUtils.ISSUER_OPENAM;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@ProtectedWithClaims(issuer = ISSUER_OPENAM)
@RestController
@RequestMapping("/synlighet")
public class SynlighetController {

    private final TilgangskontrollService tilgangskontroll;
    private final String arbeidssokerUrl;
    private final SystemUserTokenProvider systemUserTokenProvider;
    private final AktorOppslagClient aktorOppslagClient;

    public SynlighetController(
            TilgangskontrollService tilgangskontroll,
            @Value("${arbeidssoker.url}") String arbeidssokerUrl,
            SystemUserTokenProvider systemUserTokenProvider,
            AktorOppslagClient aktorOppslagClient
    ) {
        this.tilgangskontroll = tilgangskontroll;
        this.arbeidssokerUrl = arbeidssokerUrl;
        this.aktorOppslagClient = aktorOppslagClient;
        this.systemUserTokenProvider = systemUserTokenProvider;
    }

    @GetMapping("/{aktørId}")
    public ResponseEntity<?> harCvOgJobbønsker(@PathVariable("aktørId") String aktørId) {
        loggBrukAvEndepunkt();
        tilgangskontroll.sjekkLesetilgangTilKandidat(aktørId);

        Fnr fnr = aktorOppslagClient.hentFnr(new AktorId(aktørId));
        RestTemplate restTemplate = new RestTemplate();
        final String url = arbeidssokerUrl + "/rest/v2/arbeidssoker/" + fnr.get() + "?erManuell=false";
        final HttpMethod httpMethod = HttpMethod.GET;
        try {
            ResponseEntity<ArbeidssøkerResponse> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    bearerToken(),
                    ArbeidssøkerResponse.class
            );
            return HarCvOgJobbønskerResponse.fra(response.getBody());

        } catch (HttpClientErrorException e) {
            final String baseMsg = "Forsøkte å spørre Arbeidsplassen om en kandidat har CV og jobbønsker. Brukte HTTP-metode " + httpMethod + " på URL [" + maskerFnr(url) + "]";
            final String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().equals(NOT_FOUND)) {
                if (responseBody.contains("CV finnes ikke")) {
                    log.debug(baseMsg + ". CV finnes ikke.");
                    return HarCvOgJobbønskerResponse.manglerCv();
                } else {
                    val msg = baseMsg + ". Uventet tekst i body i 404 respons: " + responseBody;
                    log.error(msg, e);
                    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(msg);
                }
            } else if (e.getStatusCode().equals(FORBIDDEN)) {
                if (responseBody.contains("Bruker har ikke sett hjemmel")) {
                    log.debug(baseMsg + ". Bruker har ikke sett hjemmel.");
                    return ResponseEntity.status(FORBIDDEN).body(responseBody);
                } else {
                    val msg = baseMsg + ". Uventet tekst i body i 403 respons: " + responseBody;
                    log.error(msg, e);
                    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(msg);
                }
            } else {
                String msg = baseMsg + ". " + responseBody;
                log.error(msg, e);
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(msg);
            }
        }
    }

    private static String maskerFnr(String url) {
        return url.replaceAll("/\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d", "/***********");
    }

    private HttpEntity<?> bearerToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(systemUserTokenProvider.getSystemUserToken());
        return new HttpEntity<>(headers);
    }

    @RequiredArgsConstructor
    private static class HarCvOgJobbønskerResponse {
        public final boolean harCv;
        public final boolean harJobbprofil;

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
            return ResponseEntity
                    .status(OK)
                    .body(response);
        }
    }

    @Data
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
