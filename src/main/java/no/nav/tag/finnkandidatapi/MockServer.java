package no.nav.tag.finnkandidatapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.tilgangskontroll.sts.STSToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

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
            @Value("${veilarbabac.url}") String veilarbabacUrl,
            @Value("${abac.url}") String abacUrl
    ) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockKall(stsUrl + "/sts/token", new STSToken("blabla", "", 30000));
        mockKall(veilarbabacUrl + "/person", "allow");
        mockPostKall(abacUrl, lesFilSomString("abac.json"));

        server.start();
    }

    @SneakyThrows
    private void mockKall(String url, Object body) {
        mockKall(url, objectMapper.writeValueAsString(body));
    }

    private void mockPostKall(String url, String body) {
        String path = getPath(url);
        server.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        );
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

    @SneakyThrows
    private String lesFilSomString(String filnavn) {
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("mock/" + filnavn), UTF_8);
    }
}
