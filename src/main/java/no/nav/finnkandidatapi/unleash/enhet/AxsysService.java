package no.nav.finnkandidatapi.unleash.enhet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static no.nav.finnkandidatapi.config.CacheConfig.AXSYS_CACHE;

@Component
@Slf4j
public class AxsysService {

    private final RestTemplate restTemplate;
    private String axsysUrl;

    public AxsysService(@Value("${axsys.url}") String axsysUrl) {
        this.axsysUrl = axsysUrl;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Nav-Call-Id", UUID.randomUUID().toString());
            request.getHeaders().add("Nav-Consumer-Id", "finn-kandidat-api");
            return execution.execute(request, body);
        }));
    }

    @Cacheable(AXSYS_CACHE)
    public List<NavEnhet> hentEnheterVeilederHarTilgangTil(String navIdent) {
        URI uri = UriComponentsBuilder.fromUriString(axsysUrl)
                .pathSegment(navIdent)
                .queryParam("inkluderAlleEnheter", "false")
                .build()
                .toUri();

        try {
            AxsysRespons respons = restTemplate.getForObject(uri, AxsysRespons.class);
            return respons.tilEnheter();
        } catch (RestClientException exception) {
            log.warn("Feil ved henting av enheter for ident " + navIdent, exception);
            throw exception;
        }
    }
}
