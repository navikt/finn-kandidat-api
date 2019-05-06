package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.request.Request;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.request.XacmlRequest;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.NavAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.StandardAttributter.ACTION_ID;

@Slf4j
@Component
public class AbacClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate stsBasicAuthRestTemplate;
    private final String abacUrl;
    private final String appName = "finn-kandidat-api"; // NAIS_APP_NAME

    private final String brukernavn;
    private final String passord;

    public AbacClient(
            RestTemplate stsBasicAuthRestTemplate,
            @Value("${abac.url}") String abacUrl,
            @Value("${STS_BRUKERNAVN}") String brukernavn,
            @Value("${STS_PASSORD}") String passord
    ) {
        this.stsBasicAuthRestTemplate = stsBasicAuthRestTemplate;
        this.abacUrl = abacUrl;
        this.brukernavn = brukernavn;
        this.passord = passord;
    }

    public XacmlResponse ping() {
        XacmlRequest pingRequest = getPingRequest();
        return doRequest(pingRequest);
        /*
        Decision decision = doRequest(pingRequest).getResponse().getDecision();
        if (!decision.equals(Decision.Permit)) {
            throw new IllegalStateException("Ping failed");
        }
         */

    }

    @SneakyThrows
    private XacmlResponse doRequest(XacmlRequest xacmlRequest) {
        String uriString = UriComponentsBuilder.fromHttpUrl(abacUrl)
                .toUriString();


        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(brukernavn, passord);
        headers.set("Content-Type", "application/xacml+json");

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(xacmlRequest), headers);

        log.info("abac-request: " + entity);

        String response = stsBasicAuthRestTemplate.exchange(
                uriString,
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();

        log.info("abac-response: " + response);

        return objectMapper.readValue(response, XacmlResponse.class);
    }

    private XacmlRequest getPingRequest() {
        Attributes action = new Attributes();
        action.addAttribute(ACTION_ID, "ping");

        Attributes resource = new Attributes();
        resource.addAttribute(RESOURCE_FELLES_DOMENE, "srvfinn-kandidat-api");

        Attributes environment = new Attributes();
        environment.addAttribute(ENVIRONMENT_FELLES_PEP_ID, appName);

        return new XacmlRequest().withRequest(
                Request.builder()
                        .action(action)
                        .resource(resource)
                        .environment(environment)
                        .build()
        );
    }
}
