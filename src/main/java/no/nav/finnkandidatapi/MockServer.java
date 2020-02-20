package no.nav.finnkandidatapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.finnkandidatapi.kandidat.Oppfølgingsstatus;
import no.nav.finnkandidatapi.sts.STSToken;
import no.nav.finnkandidatapi.tilgangskontroll.veilarbabac.VeilarbabacClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;

@Profile("mock")
@Component
@Slf4j
public class MockServer implements DisposableBean {

    private final WireMockServer server;

    @Autowired
    MockServer(
            @Value("${mock.port}") Integer port,
            @Value("${sts.url}") String stsUrl,
            @Value("${veilarbabac.url}") String veilarbabacUrl,
            @Value("${aktørregister.url}") String aktørregisterUrl,
            @Value("${veilarbarena.url}") String veilarbarenaUrl,
            @Value("${axsys.url}") String axsysUrl,
            @Value("${veilarboppfolging.url}") String veilarboppfølgingUrl
    ) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockKall(veilarbabacUrl + "/person", VeilarbabacClient.PERMIT_RESPONSE);
        mockKall(stsUrl + "/sts/token", new STSToken("fdg", "asfsdg", 325));
        mockAktørregister(aktørregisterUrl);
        mockVeilarbarena(veilarbarenaUrl);
        mockAxsys(axsysUrl);
        mockKall(veilarboppfølgingUrl + "/underoppfolging", Oppfølgingsstatus.builder().underOppfolging(true).build());

        server.start();
    }

    private void mockAktørregister(String aktørregisterUrl) {
        Map<String, StringValuePattern> aktørId = new HashMap<>();
        aktørId.put("identgruppe", WireMock.equalTo("AktoerId"));

        mockKall(aktørregisterUrl + "/identer" + "?identgruppe=AktoerId&gjeldende=true",
                aktørId,
                "{\n" +
                " \"01065500791\": {\n" +
                "   \"identer\": [\n" +
                "     {\n" +
                "       \"ident\": \"1856024171652\",\n" +
                "       \"identgruppe\": \"AktoerId\",\n" +
                "       \"gjeldende\": true\n" +
                "     }\n" +
                "   ],\n" +
                "   \"feilmelding\": null\n" +
                " }\n" +
                "}");

        Map<String, StringValuePattern> norskIdent = new HashMap<>();
        norskIdent.put("identgruppe", WireMock.equalTo("NorskIdent"));
        mockKall(aktørregisterUrl + "/identer" + "?identgruppe=NorskIdent&gjeldende=true",
                norskIdent,
                "{\n" +
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

    private void mockVeilarbarena(String veilarbarenaUrl) {
        String body = (
            "{\n" +
                "\"aktoerid\": null," +
                "\"fodselsnr\": \"01065500791\"," +
                "\"formidlingsgruppekode\": \"ARBS\"," +
                "\"iserv_fra_dato\": null," +
                "\"etternavn\": null," +
                "\"fornavn\": null," +
                "\"nav_kontor\": \"0101\"," +
                "\"kvalifiseringsgruppekode\": \"BFORM\"," +
                "\"rettighetsgruppekode\": \"DAGP\"," +
                "\"hovedmaalkode\": \"SKAFFEA\"," +
                "\"sikkerhetstiltak_type_kode\": null," +
                "\"fr_kode\": null," +
                "\"har_oppfolgingssak\": true," +
                "\"sperret_ansatt\": false," +
                "\"er_doed\": false," +
                "\"doed_fra_dato\": null," +
                "\"endret_dato\": null" +
            "}"
        );

        String path = getPath(veilarbarenaUrl + "/oppfolgingsbruker");
        server.stubFor(
                get(WireMock.urlPathMatching(path + "/[0-9]+")).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        );

    }

    private void mockAxsys(String axsysUrl) {
        server.stubFor(
                get(WireMock.urlPathMatching(getPath(axsysUrl) + "/[A-Z][0-9]{6}")).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody("{\"enheter\":[{\"enhetId\":\"0213\",\"fagomrader\":[\"ABC\",\"DEF\",\"GHI\",\"JKL\",\"MNO\",\"PQR\",\"STU\",\"WXY\"],\"navn\":\"NAV Ski\"},{\"enhetId\":\"0315\",\"fagomrader\":[\"ABC\",\"DEF\",\"GHI\",\"JKL\",\"MNO\",\"PQR\",\"STU\",\"WXY\"],\"navn\":\"NAV Grünerløkka\"}]}")
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
                get(WireMock.urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(body)
                )
        );
    }

    private void mockKall(String url, Map<String, StringValuePattern> params, String body) {
        String path = getPath(url);
        server.stubFor(
                get(WireMock.urlPathEqualTo(path)).withQueryParams(params).willReturn(WireMock.aResponse()
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
