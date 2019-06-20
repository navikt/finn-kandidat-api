package no.nav.tag.finnkandidatapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;

import static no.nav.tag.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient.PERMIT_RESPONSE;

@Profile("mock")
@Component
@Slf4j
public class MockServer {
    private final WireMockServer server;

    @Autowired
    MockServer(
            @Value("${mock.port}") Integer port,
            @Value("${sts.url}") String stsUrl,
            @Value("${veilarbabac.url}") String veilarbabacUrl,
            @Value("${aktørregister.url}") String aktørregisterUrl
    ) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockKall(veilarbabacUrl + "/person", PERMIT_RESPONSE);
        mockKall(stsUrl + "/sts/token", new STSToken("fdg", "asfsdg", 325));
        mockAktørregister(aktørregisterUrl);

        server.start();
    }

    private void mockAktørregister(@Value("${aktørregister.url}") String aktørregisterUrl) {
        mockKall(aktørregisterUrl + "/identer" + "?identgruppe=NorskIdent&gjeldende=true", "{\n" +
                " \"1856024171652\": {\n" +
                "   \"identer\": [\n" +
                "     {\n" +
                "       \"ident\": \"01065500791\",\n" +
                "       \"identgruppe\": \"NorskIdent\",\n" +
                "       \"gjeldende\": true\n" +
                "     }\n" +
                "   ],\n" +
                "   \"feilmelding\": null\n" +
                " }\n" +
                "}");
    }

    @SneakyThrows
    private void mockKall(String url, Object body) {
        ObjectMapper objectMapper = new ObjectMapper();
        mockKall(url, objectMapper.writeValueAsString(body));
    }

    private void mockKall(String url, String body) {
        String path = getPath(url);
        server.stubFor(
                WireMock.get(WireMock.urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        );
    }

    @SneakyThrows
    private String getPath(String url) {
        return new URL(url).getPath();
    }
}
