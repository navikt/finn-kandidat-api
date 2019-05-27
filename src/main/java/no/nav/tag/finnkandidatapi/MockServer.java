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

@Profile("mock")
@Component
@Slf4j
public class MockServer {
    private final WireMockServer server;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockServer(
            @Value("${mock.port}") Integer port,
            @Value("${sts.url}") String stsUrl,
            @Value("${veilarbabac.url}") String veilarbabacUrl
    ) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockKall(veilarbabacUrl + "/person", "allow");
        mockKall(stsUrl + "/sts/token", new STSToken("fdg", "asfsdg", 325));

        server.start();
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
