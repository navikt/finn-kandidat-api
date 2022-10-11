package no.nav.finnkandidatapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Profile("mock")
@Component
@Slf4j
public class MockServer implements DisposableBean {

    private final WireMockServer server;

    @Autowired
    MockServer(@Value("${mock.port}") Integer port,
               @Value("${abac.url}") String abacUrl) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockAbac(abacUrl);
        mockPdl();

        server.start();
    }

    private void mockAbac(String abacUrl) {
        String path = getPath(abacUrl);
        server.stubFor(
                post(urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody("{\n" +
                                "  \"Response\": {\n" +
                                "    \"Decision\": \"Permit\"\n" +
                                "  }\n" +
                                "}")
                )
        );
    }

    private void mockPdl() {

        // fnr til aktørId
        server.stubFor(
                post(urlPathEqualTo("/graphql"))
                        .withRequestBody(equalToJson("{\"query\":\"query($ident: ID!) {    hentIdenter(ident: $ident, grupper: [AKTORID], historikk: false) {        identer {            ident        }    }}\",\"variables\":{\"ident\":\"01065500791\"}}"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.OK.value())
                                .withBody("{\"data\": {\"hentIdenter\": {\"identer\": [{\"ident\": \"1856024171652\"}]}}}")
                        )
        );

        // aktørId til fnr
        server.stubFor(
                post(urlPathEqualTo("/graphql"))
                        .withRequestBody(equalToJson("{\"query\":\"query($ident: ID!) {    hentIdenter(ident: $ident, grupper: [FOLKEREGISTERIDENT], historikk: false) {        identer {            ident        }    }}\",\"variables\":{\"ident\":\"1856024171652\"}}"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.OK.value())
                                .withBody("{\"data\": {\"hentIdenter\": {\"identer\": [{\"ident\": \"01065500791\"}]}}}")
                        )
        );
    }

    @SneakyThrows
    private void mockKall(String url, Object body) {
        ObjectMapper objectMapper = new ObjectMapper();
        mockKall(url, objectMapper.writeValueAsString(body));
    }

    private void mockKall(String url, String body) {
        String path = getPath(url);
        server.stubFor(
                get(urlPathEqualTo(path)).willReturn(WireMock.aResponse()
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

    @Override
    public void destroy() {
        server.shutdown();
    }
}
