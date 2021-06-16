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
               @Value("${abac.url}") String abacUrl,
               @Value("${arbeidssoker.url}") String pamCvApiUrl) {
        log.info("Starter mockserver");

        this.server =  new WireMockServer(port);

        mockAbac(abacUrl);
        mockPamCvApi(pamCvApiUrl);
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

    private void mockPamCvApi(String pamCvApiUrl) {
        String path = getPath(pamCvApiUrl + "/rest/v2/arbeidssoker/1");
        server.stubFor(
                get(urlPathEqualTo(path)).willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(
                                "{\n" +
                                        "    \"sistEndret\": \"2021-06-15T14:09:22.434703+02:00\",\n" +
                                        "    \"synligForArbeidsgiver\": false,\n" +
                                        "    \"sistEndretAvNav\": false,\n" +
                                        "    \"sammendrag\": \"Jeg har gjort litt av hvert og har mange fine egenskaper\",\n" +
                                        "    \"arbeidserfaring\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Rektor\",\n" +
                                        "            \"arbeidsgiver\": \"Drammen barneskole\",\n" +
                                        "            \"sted\": null,\n" +
                                        "            \"beskrivelse\": \"Litt forskjellig rektorrelatert\",\n" +
                                        "            \"fraDato\": \"2004-08\",\n" +
                                        "            \"tilDato\": \"2008-08\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"utdanning\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Bachelor i historie\",\n" +
                                        "            \"studiested\": \"Universitetet i Tromsø\",\n" +
                                        "            \"beskrivelse\": \"Historie\",\n" +
                                        "            \"fraDato\": \"1980-08\",\n" +
                                        "            \"tilDato\": \"1984-06\"\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Danselinja\",\n" +
                                        "            \"studiested\": \"Drammen forlkehøgskole\",\n" +
                                        "            \"beskrivelse\": \"Dans\",\n" +
                                        "            \"fraDato\": \"1979-07\",\n" +
                                        "            \"tilDato\": \"1980-05\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"fagdokumentasjoner\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Svennebrev baker\",\n" +
                                        "            \"type\": \"SVENNEBREV_FAGBREV\"\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Fagbrev aluminiumskonstruktør\",\n" +
                                        "            \"type\": \"SVENNEBREV_FAGBREV\"\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Mesterbrev anleggsgartner\",\n" +
                                        "            \"type\": \"MESTERBREV\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"godkjenninger\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Skipskokk\",\n" +
                                        "            \"utsteder\": null,\n" +
                                        "            \"gjennomfortDato\": \"2021-06\",\n" +
                                        "            \"utloperDato\": \"2025-10\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"annenErfaring\": [\n" +
                                        "        {\n" +
                                        "            \"rolle\": \"Fotballtrener\",\n" +
                                        "            \"beskrivelse\": \"10 års erfaring som fotballtrener for barn\",\n" +
                                        "            \"fraDato\": null,\n" +
                                        "            \"tilDato\": null\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"forerkort\": [\n" +
                                        "        {\n" +
                                        "            \"klasse\": \"A - Motorsykkel\",\n" +
                                        "            \"fraDato\": null,\n" +
                                        "            \"utloperDato\": null\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"klasse\": \"B - Personbil\",\n" +
                                        "            \"fraDato\": null,\n" +
                                        "            \"utloperDato\": null\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"klasse\": \"D - Buss\",\n" +
                                        "            \"fraDato\": \"2010-02-15\",\n" +
                                        "            \"utloperDato\": \"2025-10-15\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"kurs\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Franskkurs\",\n" +
                                        "            \"arrangor\": \"Fransk i Norge AS\",\n" +
                                        "            \"fraDato\": null,\n" +
                                        "            \"varighet\": {\n" +
                                        "                \"varighet\": null,\n" +
                                        "                \"tidsenhet\": null\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"sertifikater\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Renholdsattest\",\n" +
                                        "            \"utsteder\": null,\n" +
                                        "            \"gjennomfortDato\": \"2011-01\",\n" +
                                        "            \"utloperDato\": null\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"andreGodkjenninger\": [\n" +
                                        "        {\n" +
                                        "            \"tittel\": \"Renholdsattest\",\n" +
                                        "            \"utsteder\": null,\n" +
                                        "            \"gjennomfortDato\": \"2011-01\",\n" +
                                        "            \"utloperDato\": null\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"sprak\": [\n" +
                                        "        {\n" +
                                        "            \"sprak\": \"Engelsk\",\n" +
                                        "            \"muntligNiva\": \"IKKE_OPPGITT\",\n" +
                                        "            \"skriftligNiva\": \"IKKE_OPPGITT\"\n" +
                                        "        },\n" +
                                        "        {\n" +
                                        "            \"sprak\": \"Norsk tegnspråk\",\n" +
                                        "            \"muntligNiva\": \"IKKE_OPPGITT\",\n" +
                                        "            \"skriftligNiva\": \"IKKE_OPPGITT\"\n" +
                                        "        }\n" +
                                        "    ],\n" +
                                        "    \"jobbprofil\": {\n" +
                                        "        \"sistEndret\": \"2021-06-15T14:09:22.301432+02:00\",\n" +
                                        "        \"onsketYrke\": [\n" +
                                        "            {\n" +
                                        "                \"tittel\": \"Sanger\"\n" +
                                        "            }\n" +
                                        "        ],\n" +
                                        "        \"onsketArbeidssted\": [\n" +
                                        "            {\n" +
                                        "                \"stedsnavn\": \"Valle\"\n" +
                                        "            }\n" +
                                        "        ],\n" +
                                        "        \"onsketAnsettelsesform\": [\n" +
                                        "            {\n" +
                                        "                \"tittel\": \"FAST\"\n" +
                                        "            }\n" +
                                        "        ],\n" +
                                        "        \"onsketArbeidstidsordning\": [\n" +
                                        "            {\n" +
                                        "                \"tittel\": \"DAGTID\"\n" +
                                        "            }\n" +
                                        "        ],\n" +
                                        "        \"heltidDeltid\": {\n" +
                                        "            \"heltid\": true,\n" +
                                        "            \"deltid\": false\n" +
                                        "        },\n" +
                                        "        \"kompetanse\": [\n" +
                                        "            {\n" +
                                        "                \"tittel\": \"Elektronikk\"\n" +
                                        "            },\n" +
                                        "            {\n" +
                                        "                \"tittel\": \"Sangteknikk\"\n" +
                                        "            }\n" +
                                        "        ]\n" +
                                        "    }\n" +
                                        "}"
                        )
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
