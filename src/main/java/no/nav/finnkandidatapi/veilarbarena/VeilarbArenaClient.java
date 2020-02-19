package no.nav.finnkandidatapi.veilarbarena;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.FinnKandidatException;
import no.nav.finnkandidatapi.tilgangskontroll.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class VeilarbArenaClient {

    private static final String IKKE_REGISTRERT_I_VEILARBARENA = "finnkandidat.ikke-registrert-i-veilarbarena";

    private final RestTemplate restTemplate;
    private final String veilarbarenaUrl;
    private final TokenUtils tokenUtils;
    private final MeterRegistry meterRegistry;

    public VeilarbArenaClient(
            RestTemplate restTemplate,
            @Value("${veilarbarena.url}") String veilarbarenaUrl,
            TokenUtils tokenUtils,
            MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.veilarbarenaUrl = veilarbarenaUrl;
        this.tokenUtils = tokenUtils;
        this.meterRegistry = meterRegistry;
        meterRegistry.counter(IKKE_REGISTRERT_I_VEILARBARENA);
    }

    public Oppfølgingsbruker hentOppfølgingsbruker(String fnr, String aktørId) {
        log.info("Henter oppfølgingsbruker for aktørId {}", aktørId);

        String uri = UriComponentsBuilder.fromHttpUrl(veilarbarenaUrl)
                .path("/oppfolgingsbruker/" + fnr)
                .toUriString();

        try {
            ResponseEntity<Oppfølgingsbruker> respons = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpHeadere(),
                    Oppfølgingsbruker.class
            );

            if (respons.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                meterRegistry.counter(IKKE_REGISTRERT_I_VEILARBARENA).increment();
                log.warn("Kandidat ikke registrert i veilarbarena, aktørId: {}", aktørId);
                return Oppfølgingsbruker.builder().fnr(fnr).navKontor(null).build();
            } else {
                return respons.getBody();
            }

        } catch (RestClientResponseException exception) {
            log.error("Kunne ikke hente oppfølgingsbruker fra veilarbarena, aktørId: {}", aktørId, exception);
            throw new FinnKandidatException("Kunne ikke hente oppfølgingsbruker fra veilarbarena");
        }
    }

    private HttpEntity httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtils.hentOidcToken());
        return new HttpEntity<>(headers);
    }
}
