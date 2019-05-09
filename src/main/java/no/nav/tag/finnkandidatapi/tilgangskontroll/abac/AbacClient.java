package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kandidat.Veileder;
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

import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.NavAttributter.*;
import static no.nav.tag.finnkandidatapi.tilgangskontroll.abac.StandardAttributter.ACTION_ID;

@Slf4j
@Component
public class AbacClient {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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

    public XacmlResponse sjekkTilgang(Veileder veileder, String fnr, AbacAction action) {
        // action: read, update, ping

        Attributes accessSubject = new Attributes()
                .addAttribute(StandardAttributter.SUBJECT_ID, veileder.getNavIdent())
                .addAttribute(NavAttributter.SUBJECT_FELLES_SUBJECTTYPE, "InternBruker");

        Attributes actionAttribute = new Attributes();
        actionAttribute.addAttribute(ACTION_ID, action.getAbacKode());

        Attributes resources = new Attributes();
        resources.addAttribute(RESOURCE_FELLES_DOMENE, "veilarb");
        resources.addAttribute(RESOURCE_FELLES_PERSON_FNR, fnr);
        resources.addAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_VEILARB_PERSON);

        Attributes environment = new Attributes();
        environment.addAttribute(ENVIRONMENT_FELLES_PEP_ID, appName);

        XacmlRequest request = new XacmlRequest().withRequest(
                Request.builder()
                        .action(actionAttribute)
                        .resource(resources)
                        .environment(environment)
                        .accessSubject(accessSubject)
                        .build()
        );

        return doRequest(request);
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

        String res = stsBasicAuthRestTemplate.exchange(
                uriString,
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();

        log.info("abac-response: " + res);

        return objectMapper.readValue(res, XacmlResponse.class);
    }

    private XacmlRequest getPingRequest() {
        Attributes action = new Attributes();
        action.addAttribute(ACTION_ID, "ping");

        Attributes resource = new Attributes();
        resource.addAttribute(RESOURCE_FELLES_DOMENE, "srvfinn-kandidat-api");

        Attributes environment = new Attributes();
        environment.addAttribute(ENVIRONMENT_FELLES_PEP_ID, brukernavn);

        return new XacmlRequest().withRequest(
                Request.builder()
                        .action(action)
                        .resource(resource)
                        .environment(environment)
                        .build()
        );
    }
}
